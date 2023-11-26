package ro.builditsmart.connectors.tapo.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ro.builditsmart.connectors.tapo.config.StaticBeans;
import ro.builditsmart.connectors.tapo.util.TapoCrypto;
import ro.builditsmart.connectors.tapo.util.TapoUtils;
import ro.builditsmart.models.tapo.TapoColor;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;
import ro.builditsmart.models.tapo.TapoSetDeviceState;
import ro.builditsmart.models.tapo.exceptions.*;
import ro.builditsmart.models.tapo.method.requests.*;
import ro.builditsmart.models.tapo.method.responses.*;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ro.builditsmart.connectors.tapo.protocol.MethodActions.*;
import static ro.builditsmart.connectors.tapo.util.TapoUtils.*;

@Service
@Slf4j
public class KlapDeviceClient implements ITapoDeviceClient {

    public TapoDeviceProtocol getProtocol() {
        return TapoDeviceProtocol.Klap;
    }

    private static class KlapHandshakeKey {

        private final String sessionCookie;
        private final Long timeout;
        private final Date issueTime;
        private final byte[] remoteSeed;
        private final byte[] authHash;

        public KlapHandshakeKey(String sessionCookie, Long timeout, Date issueTime, byte[] remoteSeed, byte[] authHash) {
            this.sessionCookie = sessionCookie;
            this.timeout = timeout;
            this.issueTime = issueTime;
            this.remoteSeed = remoteSeed;
            this.authHash = authHash;
        }

        public String getSessionCookie() {
            return sessionCookie;
        }

        public Long getTimeout() {
            return timeout;
        }

        public Date getIssueTime() {
            return issueTime;
        }

        public byte[] getRemoteSeed() {
            return remoteSeed;
        }

        public byte[] getAuthHash() {
            return authHash;
        }
    }

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public KlapDeviceClient() {
        this.objectMapper = StaticBeans.jsonMapper();
        this.httpClient = StaticBeans.httpClient();
    }

    public CompletableFuture<TapoDeviceKey> loginByIpAsync(String deviceIp, String username, String password) {
        Objects.requireNonNull(deviceIp, "deviceIp cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
        Objects.requireNonNull(password, "password cannot be null");
        byte[] localSeed = TapoCrypto.generateRandomBytes(16);
        KlapHandshakeKey handshake1 = klapHandshake1Async(deviceIp, username, password, localSeed).join();
        KlapCipher klapCipher = klapHandshake2Async(deviceIp, handshake1.getSessionCookie(), localSeed, handshake1.getRemoteSeed(), handshake1.getAuthHash()).join();
        Duration timeout = Duration.of(handshake1.getTimeout(), ChronoUnit.SECONDS);
        return CompletableFuture.completedFuture(new KlapDeviceKey(deviceIp, handshake1.getSessionCookie(), timeout, handshake1.getIssueTime(), klapCipher));
    }

    @Override
    public boolean pingDevice(String deviceIp) {
        try {
            InetAddress address = InetAddress.getByName(deviceIp);
            return address.isReachable(2000);
        } catch (Exception e) {
            log.error("Inet Address throws: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CompletableFuture<DeviceEnergyInfoResponse.DeviceEnergyInfoResult> getEnergyUsageAsync(TapoDeviceKey deviceKey) {
        return sendKlapRequest(deviceKey, get_energy_usage, DeviceEnergyInfoResponse.class);
    }

    private CompletableFuture<KlapHandshakeKey> klapHandshake1Async(String deviceIp, String username, String password, byte[] localSeed) {
        Objects.requireNonNull(deviceIp, "deviceIp cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
        Objects.requireNonNull(password, "password cannot be null");
        Objects.requireNonNull(localSeed, "localSeed cannot be null");
        RequestBody requestContent = RequestBody.create(localSeed);

        String url = "http://" + deviceIp + "/app/handshake1";
        log.debug("Sending bytes '{}' to '{}' ", encodeHexString(localSeed), url);
        Request request = new Request.Builder()
                .url(url)
                .post(requestContent)
                .build();
        Date requestTime = new Date();
        Call call = httpClient.newCall(request);
        try {
            Response response = call.execute();
            ResponseBody responseBody = response.body();
            byte[] responseContentBytes = Objects.requireNonNull(responseBody, "Response body should be not null").bytes();
            String body = new String(responseContentBytes, StandardCharsets.UTF_8);
            if (!response.isSuccessful()) {
                throw new HttpResponseException(response.code(), body);
            }

            responseBody.close();
            byte[] remoteSeed = truncateByteArray(responseContentBytes, 0, 16);
            byte[] serverHash = truncateByteArray(responseContentBytes, 16, responseContentBytes.length - 16);
            byte[] usernameHash = TapoCrypto.sha1Hash(username.getBytes(StandardCharsets.UTF_8));
            byte[] passwordHash = TapoCrypto.sha1Hash(password.getBytes(StandardCharsets.UTF_8));
            byte[] authHash = TapoCrypto.sha256Encode(concatBytes(usernameHash, passwordHash));
            log.debug("Remote seed is '{}' / Server hash is '{}' / Auth hash is '{}'", encodeHexString(localSeed),
                    encodeHexString(remoteSeed), encodeHexString(authHash));
            byte[] localSeedAuthHash = TapoCrypto.sha256Encode(concatBytes(localSeed, remoteSeed, authHash));
            if (Arrays.equals(localSeedAuthHash, serverHash)) {
                String sessionCookie;
                Long timeout = null;
                Map<String, List<String>> headers = response.headers().toMultimap();
                List<String> setCookieHeaders = headers.get(SET_COOKIE);
                if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
                    Map<String, String> keyValueMap = TapoUtils.obtainSetCookieHeader(setCookieHeaders);
                    if (keyValueMap.containsKey(TP_SESSION_KEY)) {
                        sessionCookie = TP_SESSION_KEY + "=" + keyValueMap.get(TP_SESSION_KEY);
                    } else {
                        return CompletableFuture.failedFuture(new TapoKlapException("Tapo login did not receive a session id."));
                    }
                    if (keyValueMap.containsKey(TIMEOUT)) {
                        timeout = Long.parseLong(keyValueMap.get(TIMEOUT));
                    }
                } else {
                    return CompletableFuture.failedFuture(new TapoNoSetCookieHeaderException("Tapo login did not receive a set-cookie header."));
                }
                return CompletableFuture.completedFuture(new KlapHandshakeKey(sessionCookie, timeout, requestTime, remoteSeed, authHash));
            } else {
                throw new TapoInvalidRequestException(TapoException.InvalidRequestOrCredentialsErrorCode, "Authentication hash does not match server hash.");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<KlapCipher> klapHandshake2Async(String deviceIp, String sessionCookie, byte[] localSeed, byte[] remoteSeed, byte[] authHash) {
        Objects.requireNonNull(deviceIp, "deviceIp cannot be null");
        Objects.requireNonNull(sessionCookie, "sessionCookie cannot be null");
        Objects.requireNonNull(localSeed, "localSeed cannot be null");
        Objects.requireNonNull(remoteSeed, "remoteSeed cannot be null");
        Objects.requireNonNull(authHash, "authHash cannot be null");
        try {
            byte[] payload = TapoCrypto.sha256Encode(concatBytes(remoteSeed, localSeed, authHash));
            RequestBody requestContent = RequestBody.create(payload /*, MediaType.parse("application/octet-stream")*/);
            String url = "http://" + deviceIp + "/app/handshake2";
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(requestContent);
            requestBuilder.header(CONTENT_TYPE, APPLICATION_JSON);
            requestBuilder.header(ACCEPT, APPLICATION_JSON);
            requestBuilder.header(COOKIE, sessionCookie);
            Request request = requestBuilder.build();
            Call call = httpClient.newCall(request);
            Response response = call.execute();
            if (!response.isSuccessful()) {
                return CompletableFuture.failedFuture(new HttpResponseException(response.code(), Objects.requireNonNull(response.body(), "Response body should be not null").string()));
            }
            response.close();
            return CompletableFuture.completedFuture(new KlapCipher(localSeed, remoteSeed, authHash));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceRunningInfoAsync(TapoDeviceKey deviceKey) {
        return sendKlapRequest(deviceKey, get_device_running_info, DeviceGetInfoResponse.class);
    }

    public CompletableFuture<DeviceGetUsageResponse.DeviceGetUsageResult> getDeviceUsageAsync(TapoDeviceKey deviceKey) {
        return sendKlapRequest(deviceKey, get_device_usage, DeviceGetUsageResponse.class);
    }

    public CompletableFuture<DeviceGetLedInfoResponse.DeviceGetLedInfoResult> getLedInfoAsync(TapoDeviceKey deviceKey) {
        return sendKlapRequest(deviceKey, get_led_info, DeviceGetLedInfoResponse.class);
    }

    @Override
    public CompletableFuture<WirelessScanInfoResponse.WirelessScanInfoResult> getWirelessScanInfoAsync(TapoDeviceKey deviceKey) {
        return sendKlapRequest(deviceKey, get_wireless_scan_info, WirelessScanInfoResponse.class);
    }

    @Override
    public CompletableFuture<DeviceCountdownRulesResponse.DeviceCountdownRulesResult> getCountdownRulesAsync(TapoDeviceKey deviceKey) {
        return sendKlapRequest(deviceKey, get_countdown_rules, DeviceCountdownRulesResponse.class);
    }

    @Override
    public CompletableFuture<ScheduleRuleResponse.ScheduleRuleResult> getScheduleRulesAsync(TapoDeviceKey deviceKey, ScheduleRuleRequest params) {
        try {
            KlapDeviceKey protocol = deviceKey.toProtocol(KlapDeviceKey.class);
            TapoRequest<ScheduleRuleRequest> request = new TapoRequest<>();
            request.setMethod(getName(get_schedule_rules));
            request.setParams(params);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return klapRequestAsync(ScheduleRuleResponse.class, jsonRequest, deviceKey.getDeviceIp(), deviceKey.getSessionCookie(), protocol.getKlapCipher())
                    .thenApply(TapoResponse::getResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceInfoAsync(TapoDeviceKey deviceKey) {
        return decodeNickNameAndSSID(sendKlapRequest(deviceKey, get_device_info, DeviceGetInfoResponse.class));
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setPowerAsync(TapoDeviceKey deviceKey, boolean deviceOn) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        TapoSetBulbState bulbState = new TapoSetBulbState(deviceOn);
        return setTapoBulbState(deviceKey, bulbState);
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setBrightnessAsync(TapoDeviceKey deviceKey, int brightness) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        TapoSetBulbState bulbState = new TapoSetBulbState(brightness, true);
        return setTapoBulbState(deviceKey, bulbState);
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setColorAsync(TapoDeviceKey deviceKey, TapoColor color) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        Objects.requireNonNull(color, "color cannot be null");
        TapoSetBulbState bulbState = new TapoSetBulbState(color, true);
        return setTapoBulbState(deviceKey, bulbState);
    }

    private CompletableFuture<StatusTapoResponse> setTapoBulbState(TapoDeviceKey deviceKey, TapoSetBulbState bulbState) {
        try {
            KlapDeviceKey protocol = deviceKey.toProtocol(KlapDeviceKey.class);
            TapoRequest<TapoSetBulbState> request = new TapoRequest<>();
            request.setMethod(getName(set_device_info));
            request.setParams(bulbState);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return klapRequestAsync(StatusTapoResponse.class, jsonRequest, deviceKey.getDeviceIp(), deviceKey.getSessionCookie(), protocol.getKlapCipher());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public <TState extends TapoSetDeviceState> CompletableFuture<StatusTapoResponse> setStateAsync(TapoDeviceKey deviceKey, TState state) {
        try {
            KlapDeviceKey protocol = deviceKey.toProtocol(KlapDeviceKey.class);
            TapoRequest<TapoSetDeviceState> request = new TapoRequest<>();
            request.setMethod(getName(set_device_info));
            request.setParams(state);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return klapRequestAsync(StatusTapoResponse.class, jsonRequest, deviceKey.getDeviceIp(), deviceKey.getSessionCookie(), protocol.getKlapCipher());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setStateWithDelay(TapoDeviceKey deviceKey, Boolean deviceOn, Integer delay) {
        try {
            KlapDeviceKey protocol = deviceKey.toProtocol(KlapDeviceKey.class);
            var request = new TapoRequest<CountdownRuleDTO>();
            request.setMethod(getName(add_countdown_rule));
            CountdownRuleDTO rule = CountdownRuleDTO.builder()
                    .enable(true)
                    .remain(delay)
                    .delay(delay)
                    .desiredStates(CountdownRuleDTO.DesiredStates.builder()
                            .on(deviceOn)
                            .build())
                    .build();
            request.setParams(rule);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return klapRequestAsync(StatusTapoResponse.class, jsonRequest, deviceKey.getDeviceIp(), deviceKey.getSessionCookie(), protocol.getKlapCipher());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<SetAliasResponse.SetAliasResult> setAliasWithDelay(TapoDeviceKey deviceKey, SetAliasRequest newAlias) {
        try {
            KlapDeviceKey protocol = deviceKey.toProtocol(KlapDeviceKey.class);
            var request = new TapoRequest<SetAliasRequest>();
            request.setMethod(getName(set_device_info));
            request.setParams(newAlias);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return klapRequestAsync(SetAliasResponse.class, jsonRequest, deviceKey.getDeviceIp(), deviceKey.getSessionCookie(), protocol.getKlapCipher())
                    .thenApply(TapoResponse::getResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    protected <TResponse extends TapoResponse<?>> CompletableFuture<TResponse> klapRequestAsync(Class<TResponse> responseType, String deviceRequest, String deviceIp, String sessionCookie, KlapCipher klapCipher) {
        Objects.requireNonNull(deviceIp, "deviceIp cannot be null");
        try {
            byte[] payload = klapCipher.encrypt(deviceRequest);
            RequestBody requestContent = RequestBody.create(payload);
            String url = "http://" + deviceIp + "/app/request?seq=" + klapCipher.getSeq();
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(requestContent);
            if (sessionCookie != null) {
                requestBuilder.header(COOKIE, sessionCookie);
            }
            Request request = requestBuilder.build();
            Response response = httpClient.newCall(request).execute();
            byte[] responseContentBytes = Objects.requireNonNull(response.body(), "Body should be not null!").bytes();
            Objects.requireNonNull(response.body(), "Body should be not null!").close();
            String body = new String(responseContentBytes, StandardCharsets.UTF_8);
            if (!response.isSuccessful()) {
                return CompletableFuture.failedFuture(new HttpResponseException(response.code(), body));
            }
            String decryptedString = klapCipher.decrypt(responseContentBytes);
            log.info("KlapRequestAsync:\nRequest: {}\nResponse: {}", deviceRequest, decryptedString);
            TapoResponse<TResponse> responseJson = objectMapper.readValue(decryptedString, TapoResponse.class);
            if (responseJson == null) {
                return CompletableFuture.failedFuture(new TapoJsonException("Failed to deserialize because the responseJson was null."));
            } else {
                TapoException.throwFromErrorCode(responseJson.getErrorCode());
                TResponse decryptedResponseJson = objectMapper.readValue(decryptedString, responseType);
                if (decryptedResponseJson == null) {
                    throw new TapoJsonException("Failed to deserialize " + decryptedString);
                } else {
                    TapoException.throwFromErrorCode(decryptedResponseJson.getErrorCode());
                    return CompletableFuture.completedFuture(decryptedResponseJson);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private <TResult, ClassType extends TapoResponse<TResult>> CompletableFuture<TResult> sendKlapRequest(TapoDeviceKey deviceKey, MethodActions method, Class<ClassType> tClass) {
        KlapDeviceKey klapDeviceKey = (KlapDeviceKey) deviceKey;
        TapoRequest<?> request = new TapoRequest<>();
        request.setMethod(getName(method));
        String jsonRequest;
        try {
            jsonRequest = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
        ClassType value;
        try {
            value = klapRequestAsync(tClass, jsonRequest, deviceKey.getDeviceIp(), deviceKey.getSessionCookie(), klapDeviceKey.getKlapCipher()).get();
            return CompletableFuture.completedFuture(value.getResult());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

}
