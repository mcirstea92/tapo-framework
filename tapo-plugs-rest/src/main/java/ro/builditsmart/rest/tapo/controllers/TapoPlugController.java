package ro.builditsmart.rest.tapo.controllers;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.builditsmart.connectors.tapo.protocol.ITapoDeviceClient;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.method.requests.ScheduleRuleRequest;
import ro.builditsmart.models.tapo.method.requests.SetAliasRequest;
import ro.builditsmart.models.tapo.method.requests.TapoDelayedRequest;
import ro.builditsmart.models.tapo.method.requests.TapoSetPlugDeviceState;
import ro.builditsmart.models.tapo.method.responses.*;
import ro.builditsmart.rest.tapo.services.SessionManagerService;
import ro.builditsmart.rest.tapo.services.TapoClientService;
import ro.builditsmart.rest.tapo.utils.KeyValue;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static ro.builditsmart.rest.tapo.utils.Constants.unauthorizedResponseEntity;

@CrossOrigin(origins = "http://localhost:8081")
@RestController("plug-controller")
@RequestMapping("/plug")
@Slf4j
public class TapoPlugController {

    private final TapoClientService clientService;

    private final SessionManagerService sessionManagerService;

    public TapoPlugController(TapoClientService clientService, SessionManagerService sessionManagerService) {
        this.clientService = clientService;
        this.sessionManagerService = sessionManagerService;
    }

    private ITapoDeviceClient getClient(KeyValue<TapoDeviceKey, LoginResponse> session) {
        return clientService.getClient(session.getValue().getDeviceProtocol().getAlias());
    }

    @PostMapping("setPower")
    public ResponseEntity<StatusTapoResponse> setPower(@RequestBody TapoSetPlugDeviceState body, @RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> getClient(s).setPowerAsync(s.getKey(), body.getDeviceOn()));
    }

    @PostMapping("restart")
    public ResponseEntity<StatusTapoResponse> restartDevice(@RequestBody TapoDelayedRequest body, @RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> {
            getClient(s).setStateWithDelay(s.getKey(), true, body.getSeconds_delay());
            return getClient(s).setPowerAsync(s.getKey(), false);
        });
    }

    @PostMapping("addCountdownRule")
    public ResponseEntity<StatusTapoResponse> changeStateInSeconds(@RequestBody TapoDelayedRequest body, @RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> getClient(s).setStateWithDelay(s.getKey(), body.getState(), body.getSeconds_delay()));
    }

    @PostMapping("setAlias")
    public ResponseEntity<SetAliasResponse.SetAliasResult> setAlias(@RequestBody SetAliasRequest body, @RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> getClient(s).setAliasWithDelay(s.getKey(), body));
    }

    @GetMapping("wirelessScanInfo")
    public ResponseEntity<WirelessScanInfoResponse.WirelessScanInfoResult> getWirelessScanInfo(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> getClient(s).getWirelessScanInfoAsync(s.getKey()));
    }

    @GetMapping("ledInfo")
    public ResponseEntity<DeviceGetLedInfoResponse.DeviceGetLedInfoResult> getLedInfo(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> getClient(s).getLedInfoAsync(s.getKey()));
    }

    @GetMapping("deviceUsage")
    public ResponseEntity<DeviceGetUsageResponse.DeviceGetUsageResult> getDeviceUsage(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return this.getResponseEntity(cookie, s -> getClient(s).getDeviceUsageAsync(s.getKey()));
    }

    @GetMapping("deviceInfo")
    public ResponseEntity<DeviceGetInfoResponse.DeviceGetInfoResult> getEnergyUsage(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return getResponseEntity(cookie, session -> getClient(session).getDeviceInfoAsync(session.getKey()));
    }

    @GetMapping("energyInfo")
    public ResponseEntity<DeviceEnergyInfoResponse.DeviceEnergyInfoResult> getEnergyInfo(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return getResponseEntity(cookie, session -> getClient(session).getEnergyUsageAsync(session.getKey()));
    }

    @GetMapping("runningUsage")
    public ResponseEntity<DeviceGetInfoResponse.DeviceGetInfoResult> getRunningUsage(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return getResponseEntity(cookie, session -> getClient(session).getDeviceRunningInfoAsync(session.getKey()));
    }

    @PostMapping("scheduleRules")
    public ResponseEntity<ScheduleRuleResponse.ScheduleRuleResult> getScheduleRules(@RequestHeader(HttpHeaders.COOKIE) String cookie, @RequestBody ScheduleRuleRequest params) {
        return getResponseEntity(cookie, session -> getClient(session).getScheduleRulesAsync(session.getKey(), params));
    }

    @GetMapping("countdownRules")
    public ResponseEntity<DeviceCountdownRulesResponse.DeviceCountdownRulesResult> getCountdownRules(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        return getResponseEntity(cookie, session -> getClient(session).getCountdownRulesAsync(session.getKey()));
    }

    public <TResult> ResponseEntity<TResult> getResponseEntity(String cookie,
                                                               Function<KeyValue<TapoDeviceKey, LoginResponse>, CompletableFuture<TResult>> operation) {
        if (StringUtils.isEmpty(cookie)) {
            return unauthorizedResponseEntity();
        }
        var sessionInfo = sessionManagerService.getKeyPair(cookie);
        if (sessionInfo.isEmpty()) {
            return unauthorizedResponseEntity();
        }
        var result = operation.apply(sessionInfo.get()).join();
        return ResponseEntity.ok(result);
    }

    // setLedInfo
    // getPowerRules

}
