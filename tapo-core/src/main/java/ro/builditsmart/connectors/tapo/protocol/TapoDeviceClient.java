package ro.builditsmart.connectors.tapo.protocol;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ro.builditsmart.connectors.tapo.util.TapoUtils;
import ro.builditsmart.models.tapo.TapoColor;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;
import ro.builditsmart.models.tapo.TapoSetDeviceState;
import ro.builditsmart.models.tapo.exceptions.TapoProtocolDeprecatedException;
import ro.builditsmart.models.tapo.exceptions.TapoUnknownDeviceKeyProtocolException;
import ro.builditsmart.models.tapo.method.requests.SetAliasRequest;
import ro.builditsmart.models.tapo.method.responses.*;
import ro.builditsmart.models.tapo.method.requests.ScheduleRuleRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TapoDeviceClient implements ITapoDeviceClient {

    private final List<ITapoDeviceClient> deviceClients;

    @Override
    public TapoDeviceProtocol getProtocol() {
        return TapoDeviceProtocol.Multi;
    }

    public TapoDeviceClient(List<ITapoDeviceClient> deviceClients) {
        this.deviceClients = (deviceClients != null) ? deviceClients : createDefaultDeviceClients();
    }

    public TapoDeviceClient() {
        this.deviceClients = createDefaultDeviceClients();
    }

    private List<ITapoDeviceClient> createDefaultDeviceClients() {
        List<ITapoDeviceClient> clients = new ArrayList<>();
        clients.add(new KlapDeviceClient());
        clients.add(new SecurePassThroughDeviceClient("password"));
        return clients;
    }

    @Override
    public boolean pingDevice(String deviceIp) {
        return TapoUtils.pingAddress(deviceIp);
    }

    @Override
    public CompletableFuture<TapoDeviceKey> loginByIpAsync(String ipAddress, String username, String password) {
        Objects.requireNonNull(ipAddress, "ipAddress must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        for (ITapoDeviceClient client : deviceClients) {
            try {
                var result = client.loginByIpAsync(ipAddress, username, password).get();
                if (result != null) {
                    return CompletableFuture.completedFuture(result);
                }
            } catch (Exception ignored) {
            }
        }
        return CompletableFuture.failedFuture(new TapoProtocolDeprecatedException("No protocol worked for logging into the device."));
    }

    //TODO refactor all methods to use common logic like the Klap and Passthrough clients

    @Override
    public CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceInfoAsync(TapoDeviceKey deviceKey) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.getDeviceInfoAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<DeviceGetUsageResponse.DeviceGetUsageResult> getDeviceUsageAsync(TapoDeviceKey deviceKey) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.getDeviceUsageAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<DeviceGetInfoResponse.DeviceGetInfoResult> getDeviceRunningInfoAsync(TapoDeviceKey deviceKey) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.getDeviceRunningInfoAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<DeviceGetLedInfoResponse.DeviceGetLedInfoResult> getLedInfoAsync(TapoDeviceKey deviceKey) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.getLedInfoAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<WirelessScanInfoResponse.WirelessScanInfoResult> getWirelessScanInfoAsync(TapoDeviceKey deviceKey) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.getWirelessScanInfoAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<DeviceCountdownRulesResponse.DeviceCountdownRulesResult> getCountdownRulesAsync(TapoDeviceKey deviceKey) {
        if (deviceKey == null) {
            throw new IllegalArgumentException("deviceKey must not be null");
        }
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);
        if (client != null) {
            return client.getCountdownRulesAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<ScheduleRuleResponse.ScheduleRuleResult> getScheduleRulesAsync(TapoDeviceKey deviceKey, ScheduleRuleRequest params) {
        if (deviceKey == null) {
            throw new IllegalArgumentException("deviceKey must not be null");
        }
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);
        if (client != null) {
            return client.getScheduleRulesAsync(deviceKey, params);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<DeviceEnergyInfoResponse.DeviceEnergyInfoResult> getEnergyUsageAsync(TapoDeviceKey deviceKey) {
        if (deviceKey == null) {
            throw new IllegalArgumentException("deviceKey must not be null");
        }
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);
        if (client != null) {
            return client.getEnergyUsageAsync(deviceKey);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setPowerAsync(TapoDeviceKey deviceKey, boolean deviceOn) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");

        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.setPowerAsync(deviceKey, deviceOn);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setBrightnessAsync(TapoDeviceKey deviceKey, int brightness) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);
        if (client != null) {
            return client.setBrightnessAsync(deviceKey, brightness);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setColorAsync(TapoDeviceKey deviceKey, TapoColor color) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        Objects.requireNonNull(color, "color must not be null");

        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.setColorAsync(deviceKey, color);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public <TState extends TapoSetDeviceState> CompletableFuture<StatusTapoResponse> setStateAsync(TapoDeviceKey deviceKey, TState state) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        Objects.requireNonNull(state, "state must not be null");

        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.setStateAsync(deviceKey, state);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<StatusTapoResponse> setStateWithDelay(TapoDeviceKey deviceKey, Boolean deviceOn, Integer delay) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        Objects.requireNonNull(deviceOn, "deviceOn must not be null");
        Objects.requireNonNull(delay, "delay must not be null");

        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.setStateWithDelay(deviceKey, deviceOn, delay);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Override
    public CompletableFuture<SetAliasResponse.SetAliasResult> setAliasWithDelay(TapoDeviceKey deviceKey, SetAliasRequest newAlias) {
        Objects.requireNonNull(deviceKey, "deviceKey must not be null");
        Objects.requireNonNull(newAlias, "newAlias must not be null");

        ITapoDeviceClient client = getTapoDeviceClient(deviceKey);

        if (client != null) {
            return client.setAliasWithDelay(deviceKey, newAlias);
        } else {
            return CompletableFuture.failedFuture(new TapoUnknownDeviceKeyProtocolException("Unhandled device key protocol: " + deviceKey.getClass().getName() + "."));
        }
    }

    @Nullable
    private ITapoDeviceClient getTapoDeviceClient(TapoDeviceKey deviceKey) {
        return deviceClients.stream()
                .filter(c -> c.getProtocol() == deviceKey.getDeviceProtocol())
                .findFirst()
                .orElse(null);
    }


}