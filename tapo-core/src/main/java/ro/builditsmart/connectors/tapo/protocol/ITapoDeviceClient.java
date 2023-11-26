package ro.builditsmart.connectors.tapo.protocol;

import ro.builditsmart.connectors.tapo.util.TapoCrypto;
import ro.builditsmart.models.tapo.TapoColor;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;
import ro.builditsmart.models.tapo.TapoSetDeviceState;
import ro.builditsmart.models.tapo.method.requests.SetAliasRequest;
import ro.builditsmart.models.tapo.method.responses.*;
import ro.builditsmart.models.tapo.method.requests.ScheduleRuleRequest;

import java.util.concurrent.CompletableFuture;

import static ro.builditsmart.connectors.tapo.util.TapoUtils.TAPOLinkDeviceType;

public interface ITapoDeviceClient {

    String TP_SESSION_KEY = "TP_SESSIONID";
    String TERMINAL_UUID = "59e6bd51-104c-4190-bc4d-95b0f7b17282";
    String HANDSHAKE = "handshake";
    String REQUEST_TIME_MILS = "requestTimeMils";
    String TERMINAL_UUID1 = "terminalUUID";
    String COOKIE = "Cookie";
    String APPLICATION_JSON = "application/json";
    String CONTENT_TYPE = "Content-Type";
    String ACCEPT = "Accept";
    String SET_COOKIE = "Set-Cookie";
    String TIMEOUT = "TIMEOUT";

    boolean pingDevice(String deviceIp);

    TapoDeviceProtocol getProtocol();

    CompletableFuture<TapoDeviceKey> loginByIpAsync(String ipAddress, String email, String password);

    CompletableFuture<DeviceEnergyInfoResponse.DeviceEnergyInfoResult> getEnergyUsageAsync(TapoDeviceKey deviceKey);

    CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceInfoAsync(TapoDeviceKey deviceKey);

    CompletableFuture<DeviceGetUsageResponse.DeviceGetUsageResult> getDeviceUsageAsync(TapoDeviceKey deviceKey);

    CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceRunningInfoAsync(TapoDeviceKey deviceKey);

    CompletableFuture<DeviceGetLedInfoResponse.DeviceGetLedInfoResult> getLedInfoAsync(TapoDeviceKey deviceKey);

    CompletableFuture<WirelessScanInfoResponse.WirelessScanInfoResult> getWirelessScanInfoAsync(TapoDeviceKey deviceKey);

    CompletableFuture<DeviceCountdownRulesResponse.DeviceCountdownRulesResult> getCountdownRulesAsync(TapoDeviceKey deviceKey);

    CompletableFuture<ScheduleRuleResponse.ScheduleRuleResult> getScheduleRulesAsync(TapoDeviceKey deviceKey, ScheduleRuleRequest params);

    CompletableFuture<StatusTapoResponse> setPowerAsync(TapoDeviceKey deviceKey, boolean deviceOn);

    CompletableFuture<StatusTapoResponse> setBrightnessAsync(TapoDeviceKey deviceKey, int brightness);

    CompletableFuture<StatusTapoResponse> setColorAsync(TapoDeviceKey deviceKey, TapoColor color);

    <TState extends TapoSetDeviceState> CompletableFuture<StatusTapoResponse> setStateAsync(TapoDeviceKey deviceKey, TState state);

    CompletableFuture<StatusTapoResponse> setStateWithDelay(TapoDeviceKey deviceKey, Boolean deviceOn, Integer delay);

    CompletableFuture<SetAliasResponse.SetAliasResult> setAliasWithDelay(TapoDeviceKey deviceKey, SetAliasRequest newAlias);

    default CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> decodeNickNameAndSSID(CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> result) {
        result.thenApply(deviceInfo -> {
            boolean doubleDecoding = deviceInfo.getType().equals(TAPOLinkDeviceType);
            deviceInfo.setSsid(TapoCrypto.base64Decode(deviceInfo.getSsid(), doubleDecoding));
            deviceInfo.setNickname(TapoCrypto.base64Decode(deviceInfo.getNickname(), doubleDecoding));
            return deviceInfo;
        });
        return result;
    }
}
