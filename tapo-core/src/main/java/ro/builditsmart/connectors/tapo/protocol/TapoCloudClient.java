package ro.builditsmart.connectors.tapo.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ro.builditsmart.connectors.tapo.config.StaticBeans;
import ro.builditsmart.connectors.tapo.util.TapoCrypto;
import ro.builditsmart.connectors.tapo.util.TapoUtils;
import ro.builditsmart.models.tapo.cloud.CloudListDeviceResponse;
import ro.builditsmart.models.tapo.cloud.CloudLoginResponse;
import ro.builditsmart.models.tapo.cloud.CloudRefreshLoginResponse;
import ro.builditsmart.models.tapo.exceptions.HttpResponseException;
import ro.builditsmart.models.tapo.exceptions.TapoException;
import ro.builditsmart.models.tapo.exceptions.TapoJsonException;
import ro.builditsmart.models.tapo.method.requests.TapoRequest;
import ro.builditsmart.models.tapo.cloud.TapoDeviceDTO;
import ro.builditsmart.models.tapo.video.TapoVideoList;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ro.builditsmart.connectors.tapo.protocol.ITapoDeviceClient.APPLICATION_JSON;

@Service
@Slf4j
public class TapoCloudClient implements ITapoCloudClient {

    private String baseUrl;
    private String baseCareUrl;
    private String appType;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public TapoCloudClient(String appType, String baseUrl, String careUrl) {
        this.appType = appType;
        this.baseUrl = baseUrl;
        this.baseCareUrl = careUrl;
        this.objectMapper = StaticBeans.jsonMapper();
        this.httpClient = StaticBeans.httpClient();
    }

    public TapoCloudClient() {
        this.objectMapper = StaticBeans.jsonMapper();
        this.httpClient = StaticBeans.httpClient();
    }

    public CompletableFuture<CloudLoginResponse.CloudLoginResult> loginAsync(String email, String password, boolean refreshTokenNeeded) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("email and password must not be null");
        }

        // Construct the request body
        Map<String, Object> params = new HashMap<>();
        params.put("appType", appType);
        params.put("cloudUserName", email);
        params.put("cloudPassword", password);
        params.put("refreshTokenNeeded", refreshTokenNeeded);
        params.put("terminalUUID", UUID.randomUUID().toString());

        TapoRequest<Map<String, Object>> request = new TapoRequest<>();
        request.setMethod("login");
        request.setParams(params);

        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
        RequestBody requestBody = RequestBody.create(requestJson, MediaType.parse(APPLICATION_JSON));

        // Construct the HTTP request
        Request httpRequest = new Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .build();

        // Execute the HTTP request
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseContentString = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                throw new HttpResponseException(response.code(), responseContentString);
            }
            Objects.requireNonNull(response.body()).close();
            log.info("Logged in to {} with response: {}", baseUrl, responseContentString);
            CloudLoginResponse responseJson = objectMapper.readValue(responseContentString, CloudLoginResponse.class);
            if (responseJson == null) {
                throw new TapoJsonException("Failed to deserialize responseJson.");
            } else {
                TapoException.throwFromErrorCode(responseJson.getErrorCode());
                return CompletableFuture.completedFuture(responseJson.getResult());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<CloudRefreshLoginResponse.CloudRefreshLoginResult> refreshLoginAsync(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("refreshToken must not be null");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("appType", appType);
        params.put("refreshToken", refreshToken);
        params.put("terminalUUID", UUID.randomUUID().toString());
        TapoRequest<Map<String, Object>> request = new TapoRequest<>();
        request.setMethod("refreshToken");
        request.setParams(params);
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(new CloudRefreshLoginResponse.CloudRefreshLoginResult());
        }
        RequestBody requestBody = RequestBody.create(requestJson, MediaType.parse(APPLICATION_JSON));
        Request httpRequest = new Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .build();
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseContentString = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                throw new HttpResponseException(response.code(), responseContentString);
            }
            Objects.requireNonNull(response.body()).close();
            log.info("Refreshed token at {} with response: {}", baseUrl, responseContentString);
            CloudRefreshLoginResponse responseJson = objectMapper.readValue(responseContentString, CloudRefreshLoginResponse.class);
            if (responseJson == null) {
                throw new TapoJsonException("Failed to deserialize responseJson.");
            } else {
                try {
                    TapoException.throwFromErrorCode(responseJson.getErrorCode());
                    return CompletableFuture.completedFuture(responseJson.getResult());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return CompletableFuture.failedFuture(e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }


    @Override
    public CompletableFuture<CloudListDeviceResponse.CloudListDeviceResult> listDevicesAsync(String cloudToken) {
        if (cloudToken == null) {
            throw new IllegalArgumentException("cloudToken must not be null");
        }
        TapoRequest<?> request = new TapoRequest<>();
        request.setMethod("getDeviceList");
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
        RequestBody requestBody = RequestBody.create(requestJson, MediaType.parse(APPLICATION_JSON));
        String url = baseUrl + "?token=" + cloudToken;
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseContentString = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                throw new HttpResponseException(response.code(), responseContentString);
            }
            Objects.requireNonNull(response.body()).close();
            log.debug("Got devices list at {} with response: {}", url, responseContentString);
            CloudListDeviceResponse responseJson = objectMapper.readValue(responseContentString, CloudListDeviceResponse.class);
            if (responseJson == null) {
                throw new TapoJsonException("Failed to deserialize responseJson.");
            } else {
                TapoException.throwFromErrorCode(responseJson.getErrorCode());
                List<TapoDeviceDTO> deviceList = responseJson.getResult().getDeviceList();
                for (TapoDeviceDTO d : deviceList) {
                    if (TapoUtils.isTapoDevice(d.getDeviceType())) {
                        d.setAlias(TapoCrypto.base64Decode(d.getAlias(), TapoUtils.needsDoubleDecoding(d.getDeviceType())));
                    }
                }
                TapoException.throwFromErrorCode(responseJson.getErrorCode());
                return CompletableFuture.completedFuture(responseJson.getResult());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<TapoVideoList> listVideosAsync(String cloudToken) {
        if (cloudToken == null) {
            throw new IllegalArgumentException("cloudToken must not be null");
        }
        String url = baseCareUrl + "v1/videos";
        Headers headers = Headers.of("Authorization", "ut|" + cloudToken);
        Request httpRequest = new Request.Builder()
                .headers(headers)
                .url(url)
                .get()
                .build();

        X509Certificate tapoCareCertificate = TapoUtils.parseCertificate(TapoUtils.CA_STRING);

        X509TrustManager trustManager = TapoUtils.createX509TrustManager(tapoCareCertificate);
        SSLSocketFactory sslSocketFactory;
        try {
            sslSocketFactory = TapoUtils.createSslSocketFactory(tapoCareCertificate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }

        OkHttpClient httpsClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .build();

        try (Response response = httpsClient.newCall(httpRequest).execute()) {
            String responseContentString = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                throw new HttpResponseException(response.code(), responseContentString);
            }
            Objects.requireNonNull(response.body()).close();
            log.debug("Got videos from TAPO Care at {} with response: {}", url, responseContentString);
            TapoVideoList tapoVideoList = objectMapper.readValue(responseContentString, TapoVideoList.class);
            return CompletableFuture.completedFuture(tapoVideoList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

}
