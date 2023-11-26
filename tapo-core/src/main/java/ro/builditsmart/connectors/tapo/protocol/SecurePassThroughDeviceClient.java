package ro.builditsmart.connectors.tapo.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ro.builditsmart.connectors.tapo.config.StaticBeans;
import ro.builditsmart.connectors.tapo.util.RSAKeyPair;
import ro.builditsmart.connectors.tapo.util.TapoCrypto;
import ro.builditsmart.connectors.tapo.util.TapoUtils;
import ro.builditsmart.models.tapo.TapoColor;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;
import ro.builditsmart.models.tapo.TapoSetDeviceState;
import ro.builditsmart.models.tapo.exceptions.HttpResponseException;
import ro.builditsmart.models.tapo.exceptions.TapoException;
import ro.builditsmart.models.tapo.exceptions.TapoJsonException;
import ro.builditsmart.models.tapo.exceptions.TapoNoSetCookieHeaderException;
import ro.builditsmart.models.tapo.method.requests.*;
import ro.builditsmart.models.tapo.method.responses.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ro.builditsmart.connectors.tapo.protocol.MethodActions.*;

@Service
@Slf4j
public class SecurePassThroughDeviceClient implements ITapoDeviceClient {

    private final TapoDeviceProtocol protocol = TapoDeviceProtocol.SecurePassThrough;
    private final String privateKeyPassword;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    public SecurePassThroughDeviceClient() {
        this(null);
    }

    public SecurePassThroughDeviceClient(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword != null ? privateKeyPassword : java.util.UUID.randomUUID().toString();
        this.objectMapper = StaticBeans.jsonMapper();
        this.client = StaticBeans.httpClient();
    }

    public static class TapoHandshakeKey {
        private final String sessionCookie;
        private final Long timeout;
        private final Date issueTime;
        private final byte[] key;
        private final byte[] iv;

        public TapoHandshakeKey(String sessionCookie, Long timeout, Date issueTime, byte[] key, byte[] iv) {
            this.sessionCookie = sessionCookie;
            this.timeout = timeout;
            this.issueTime = issueTime;
            this.key = key;
            this.iv = iv;
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

        public byte[] getKey() {
            return key;
        }

        public byte[] getIv() {
            return iv;
        }
    }

    @Override
    public boolean pingDevice(String deviceIp) {
        return TapoUtils.pingAddress(deviceIp);
    }

    @Override
    public TapoDeviceProtocol getProtocol() {
        return protocol;
    }

    @Override
    public CompletableFuture<TapoDeviceKey> loginByIpAsync(String ipAddress, String email, String password) {
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(password, "password cannot be null");
        Objects.requireNonNull(ipAddress, "ipAddress cannot be null");
        CompletableFuture<TapoHandshakeKey> handshakeFuture = handshakeAsync(ipAddress);
        return handshakeFuture.thenCompose(result -> {
            try {
                byte[] emailBytes = email.getBytes(StandardCharsets.UTF_8);
                byte[] emailHash = TapoCrypto.sha1Hash(emailBytes);
                String emailHexString = TapoUtils.encodeHexString(emailHash);

                TapoRequest<Object> request = new TapoRequest<>();
                request.setMethod(getName(login_device));
                request.setParams(Map.of("username", TapoCrypto.base64Encode(emailHexString), "password", TapoCrypto.base64Encode(password)));
                String jsonRequest = objectMapper.writeValueAsString(request);
                return securePassThroughAsync(
                        DeviceLoginResponse.class, jsonRequest, ipAddress, null, result.getSessionCookie(), result.getKey(), result.getIv())
                        .thenApply(response -> new SecurePassThroughDeviceKey(ipAddress,
                                result.getSessionCookie(),
                                Duration.of(result.getTimeout(), ChronoUnit.SECONDS),
                                result.getIssueTime(),
                                result.getKey(),
                                result.getIv(),
                                response.getResult().getToken()));
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    @Override
    public CompletableFuture<DeviceEnergyInfoResponse.DeviceEnergyInfoResult> getEnergyUsageAsync(TapoDeviceKey deviceKey) {
        return sendSecurePassthroughRequest(deviceKey, get_energy_usage, DeviceEnergyInfoResponse.class);
    }

    @Override
    public CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceInfoAsync(TapoDeviceKey deviceKey) {
        return decodeNickNameAndSSID(sendSecurePassthroughRequest(deviceKey, get_device_info, DeviceGetInfoResponse.class));
    }

    @Override
    public CompletableFuture<DeviceGetUsageResponse.DeviceGetUsageResult> getDeviceUsageAsync(TapoDeviceKey deviceKey) {
        return sendSecurePassthroughRequest(deviceKey, get_device_usage, DeviceGetUsageResponse.class);
    }

    @Override
    public CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceRunningInfoAsync(TapoDeviceKey deviceKey) {
        return sendSecurePassthroughRequest(deviceKey, get_device_running_info, DeviceGetInfoResponse.class);
    }

    @Override
    public CompletableFuture<DeviceGetLedInfoResponse.DeviceGetLedInfoResult> getLedInfoAsync(TapoDeviceKey deviceKey) {
        return sendSecurePassthroughRequest(deviceKey, get_led_info, DeviceGetLedInfoResponse.class);
    }

    @Override
    public CompletableFuture<WirelessScanInfoResponse.WirelessScanInfoResult> getWirelessScanInfoAsync(TapoDeviceKey deviceKey) {
        return sendSecurePassthroughRequest(deviceKey, get_wireless_scan_info, WirelessScanInfoResponse.class);
    }

    @Override
    public CompletableFuture<DeviceCountdownRulesResponse.DeviceCountdownRulesResult> getCountdownRulesAsync(TapoDeviceKey deviceKey) {
        return sendSecurePassthroughRequest(deviceKey, get_countdown_rules, DeviceCountdownRulesResponse.class);
    }

    @Override
    public CompletableFuture<ScheduleRuleResponse.ScheduleRuleResult> getScheduleRulesAsync(TapoDeviceKey deviceKey, ScheduleRuleRequest params) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoRequest<ScheduleRuleRequest> request = new TapoRequest<>();
            request.setMethod(getName(get_schedule_rules));
            request.setParams(params);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return securePassThroughAsync(ScheduleRuleResponse.class, jsonRequest, deviceKey.getDeviceIp(), protocol.getToken(), deviceKey.getSessionCookie(), protocol.getKey(), protocol.getIv())
                    .thenApply(TapoResponse::getResult);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setPowerAsync(TapoDeviceKey deviceKey, boolean deviceOn) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoSetBulbState params = new TapoSetBulbState(deviceOn);
            return getTapoResponseCompletableFuture(deviceKey, protocol, params, StatusTapoResponse.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private <TParams, TResult extends TapoResponse<?>> CompletableFuture<TResult> getTapoResponseCompletableFuture(TapoDeviceKey deviceKey, SecurePassThroughDeviceKey protocol, TParams params, Class<TResult> tClass) throws JsonProcessingException {
        TapoRequest<TParams> request = new TapoRequest<>();
        request.setMethod(getName(set_device_info));
        request.setParams(params);
        String jsonRequest = objectMapper.writeValueAsString(request);
        return securePassThroughAsync(tClass, jsonRequest, deviceKey.getDeviceIp(), protocol.getToken(), deviceKey.getSessionCookie(), protocol.getKey(), protocol.getIv());
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setBrightnessAsync(TapoDeviceKey deviceKey, int brightness) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoSetBulbState params = new TapoSetBulbState(brightness, true);
            return getTapoResponseCompletableFuture(deviceKey, protocol, params, StatusTapoResponse.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setColorAsync(TapoDeviceKey deviceKey, TapoColor color) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        Objects.requireNonNull(color, "color cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoSetBulbState params = new TapoSetBulbState(color, true);
            return getTapoResponseCompletableFuture(deviceKey, protocol, params, StatusTapoResponse.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public <TState extends TapoSetDeviceState> CompletableFuture<StatusTapoResponse> setStateAsync(TapoDeviceKey deviceKey, TState state) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoRequest<TState> request = new TapoRequest<>();
            request.setMethod(getName(set_device_info));
            request.setParams(state);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return securePassThroughAsync(StatusTapoResponse.class, jsonRequest, deviceKey.getDeviceIp(), protocol.getToken(),
                    deviceKey.getSessionCookie(), protocol.getKey(), protocol.getIv());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setStateWithDelay(TapoDeviceKey deviceKey, Boolean deviceOn, Integer delay) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        Objects.requireNonNull(deviceOn, "deviceOn cannot be null");
        Objects.requireNonNull(delay, "delay cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoRequest<CountdownRuleDTO> request = new TapoRequest<>();
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
            return securePassThroughAsync(StatusTapoResponse.class, jsonRequest, deviceKey.getDeviceIp(), protocol.getToken(), deviceKey.getSessionCookie(), protocol.getKey(), protocol.getIv());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<SetAliasResponse.SetAliasResult> setAliasWithDelay(TapoDeviceKey deviceKey, SetAliasRequest newAlias) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        Objects.requireNonNull(newAlias, "newAlias cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoRequest<SetAliasRequest> request = new TapoRequest<>();
            request.setMethod(getName(set_device_info));
            request.setParams(newAlias);
            String jsonRequest = objectMapper.writeValueAsString(request);
            return securePassThroughAsync(SetAliasResponse.class, jsonRequest, deviceKey.getDeviceIp(), protocol.getToken(), deviceKey.getSessionCookie(), protocol.getKey(), protocol.getIv())
                    .thenApply(TapoResponse::getResult);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public Map<DeviceHandshakeResponse, Headers> makeHandshake(String deviceIp, RSAKeyPair keyPair) {
        Objects.requireNonNull(deviceIp, "deviceIp cannot be null");
        try {
            TapoRequest<Map<String, String>> tapoRequest = new TapoRequest<>();
            tapoRequest.setMethod(HANDSHAKE);
            tapoRequest.setParams(Map.of("key", keyPair.getPublicKey()));
            String jsonRequest = enhancePassThroughRequest(objectMapper.writeValueAsString(tapoRequest));
            jsonRequest = enhancePassThroughRequest(jsonRequest);
            RequestBody requestContent = RequestBody.create(jsonRequest, MediaType.parse("application/json"));
            String url = "http://" + deviceIp + "/app";
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestContent)
                    .build();
            //todo remove log.info("Handshake request: {} - {}", request, jsonRequest);
            try (Response response = client.newCall(request).execute()) {
                String bodyResponseString = Objects.requireNonNull(response.body()).string();
                //todo remove log.info("Got handshake response: {}", bodyResponseString);
                if (!response.isSuccessful()) {
                    throw new HttpResponseException(response.code(), bodyResponseString);
                }
                DeviceHandshakeResponse handshakeResponse = objectMapper.readValue(bodyResponseString, DeviceHandshakeResponse.class);
                Objects.requireNonNull(response.body()).close();
                return Map.of(handshakeResponse, response.headers());
            }
        } catch (Exception e) {
            return null;
        }
    }

    public TapoHandshakeKey createHandshakeKey(Map<DeviceHandshakeResponse, Headers> map, RSAKeyPair keyPair) {
        try {
            String sessionCookie;
            Long timeout = null;
            Headers headers = map.values().stream().findFirst()
                    .orElseThrow(() -> new TapoNoSetCookieHeaderException("Tapo login did not receive a set-cookie header."));
            DeviceHandshakeResponse handshakeResponse = map.keySet().stream().findFirst()
                    .orElseThrow(() -> new TapoException("No keys are present in the response map"));
            List<String> setCookieHeaders = headers.values(SET_COOKIE);
            if (!setCookieHeaders.isEmpty()) {
                Map<String, String> keyValueMap = TapoUtils.obtainSetCookieHeader(setCookieHeaders);

                if (keyValueMap.containsKey(TP_SESSION_KEY)) {
                    sessionCookie = TP_SESSION_KEY + "=" + keyValueMap.get(TP_SESSION_KEY);
                } else {
                    throw new Exception("Tapo login did not receive a session id.");
                }

                if (keyValueMap.containsKey(TIMEOUT)) {
                    timeout = Long.parseLong(keyValueMap.get(TIMEOUT));
                }
            } else {
                throw new TapoNoSetCookieHeaderException("Tapo login did not receive a set-cookie header.");
            }
            TapoException.throwFromErrorCode(handshakeResponse.getErrorCode());
            byte[] deviceKey = TapoCrypto.decryptWithPrivateKeyAndPassword(handshakeResponse.getResult().getKey(), keyPair.getPrivateKey(), privateKeyPassword);
            byte[] key = Arrays.copyOf(deviceKey, 16);
            byte[] iv = Arrays.copyOfRange(deviceKey, 16, 32);
            Date requestTime = new Date();
            return new TapoHandshakeKey(sessionCookie, timeout, requestTime, key, iv);
        } catch (Exception e) {
            return null;
        }
    }

    public CompletableFuture<TapoHandshakeKey> handshakeAsync(String deviceIp) {
        try {
            RSAKeyPair keyPair = TapoCrypto.generateKeyPair(privateKeyPassword, 1024);
            Map<DeviceHandshakeResponse, Headers> handshakeResponse = makeHandshake(deviceIp, keyPair);
            return CompletableFuture.completedFuture(createHandshakeKey(handshakeResponse, keyPair));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private String enhancePassThroughRequest(String jsonRequest) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(jsonRequest);
        ObjectNode customNode = objectMapper.createObjectNode();
        customNode.put(REQUEST_TIME_MILS, new Date().getTime());
        customNode.put(TERMINAL_UUID1, TERMINAL_UUID);
        ((ObjectNode) node).setAll(customNode);
        jsonRequest = objectMapper.writeValueAsString(node);
        return jsonRequest;
    }

    public <ClassType extends TapoResponse<?>> CompletableFuture<ClassType> securePassThroughAsync(Class<ClassType> expectedResponseClass, String deviceRequest, String deviceIp, String token, String cookie, byte[] key, byte[] iv) {
        Objects.requireNonNull(deviceRequest, "deviceRequest cannot be null");
        Objects.requireNonNull(deviceIp, "deviceIp cannot be null");
        Objects.requireNonNull(cookie, "cookie cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(iv, "iv cannot be null");
        try {
            byte[] encryptedBytes = TapoCrypto.encrypt(deviceRequest, key, iv);
            String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);

            TapoRequest<Object> tapoRequest = new TapoRequest<>();
            tapoRequest.setMethod(getName(securePassthrough));
            tapoRequest.setParams(Map.of("request", encryptedString));
            String jsonRequest = enhancePassThroughRequest(objectMapper.writeValueAsString(tapoRequest));

            RequestBody requestContent = RequestBody.create(jsonRequest, MediaType.parse(APPLICATION_JSON));
            String url = "http://" + deviceIp + "/app?token=" + (token != null ? token : "undefined");
            //todo remove log.info("Calling securePassthrough async. \nUrl: {}. \nToken: {}.\nRequest body: {}", url, token, jsonRequest);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestContent)
                    .header(COOKIE, cookie)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String responseContentString = Objects.requireNonNull(response.body(), "Body should be not null!").string();
                if (!response.isSuccessful()) {
                    throw new HttpResponseException(response.code(), responseContentString);
                }
                Objects.requireNonNull(response.body(), "Body should be not null!").close();
                DeviceSecurePassthroughResponse responseJson = objectMapper.readValue(responseContentString, DeviceSecurePassthroughResponse.class);
                if (responseJson == null) {
                    throw new TapoJsonException("Failed to deserialize " + responseJson);
                }
                TapoException.throwFromErrorCode(responseJson.getErrorCode());
                byte[] bytes = Base64.getDecoder().decode(responseJson.getResult().getResponse());
                byte[] decryptedBytes = TapoCrypto.decrypt(bytes, key, iv);
                String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
                log.info("Got response from\nUrl: {} \nRequest body: {}\nResponse body: {}", url, jsonRequest, decryptedString);
                ClassType decryptedResponseJson = objectMapper.readValue(decryptedString, expectedResponseClass);
                if (decryptedResponseJson == null) {
                    throw new TapoJsonException("Failed to deserialize " + decryptedString);
                } else {
                    TapoException.throwFromErrorCode(decryptedResponseJson.getErrorCode());
                    return CompletableFuture.completedFuture(decryptedResponseJson);
                }
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private <TResult, ClassType extends TapoResponse<TResult>> CompletableFuture<TResult> sendSecurePassthroughRequest(TapoDeviceKey deviceKey, MethodActions method, Class<ClassType> tClass) {
        Objects.requireNonNull(deviceKey, "deviceKey cannot be null");
        try {
            SecurePassThroughDeviceKey protocol = deviceKey.toProtocol(SecurePassThroughDeviceKey.class);
            TapoRequest<Object> request = new TapoRequest<>();
            request.setMethod(getName(method));
            String jsonRequest = objectMapper.writeValueAsString(request);
            return securePassThroughAsync(tClass, jsonRequest, deviceKey.getDeviceIp(), protocol.getToken(), deviceKey.getSessionCookie(), protocol.getKey(), protocol.getIv())
                    .thenApply(TapoResponse::getResult);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
