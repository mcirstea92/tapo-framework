package ro.builditsmart.connectors.tapo;

import lombok.extern.slf4j.Slf4j;
import ro.builditsmart.connectors.tapo.config.properties.TapoDeviceConfig;
import ro.builditsmart.connectors.tapo.config.properties.TestConfiguration;
import ro.builditsmart.connectors.tapo.protocol.ITapoDeviceClient;
import ro.builditsmart.models.tapo.TapoDeviceKey;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class BaseTest implements CommonsLogin {

    protected static TapoDeviceKey deviceKey;

    protected static ITapoDeviceClient client;

    protected TapoDeviceConfig testDevice;

    public BaseTest(ITapoDeviceClient _client) {
        client = _client;
    }

    public void initialize(TestConfiguration testConfiguration) {
        assertNotNull(testConfiguration, "Configuration should be not null");
        assertNotNull(testConfiguration.getDevices(), "Devices should be not null");
        assertTrue(testConfiguration.getDevices().size() > 0, "At least a device should be configured");
        testDevice = testConfiguration.getDevices().get(0);
        assertNotNull(testDevice.getName(), "Name of the first device should be not null");
        assertNotNull(testDevice.getModel(), "Model of the first device should be not null");
        assertNotNull(testDevice.getType(), "Type of the first device should be not null");
        assertNotNull(testDevice.getProtocol(), "Protocol of the first device should be not null");
        loginByIpAsync(testDevice.getAddress(), testConfiguration.getEmail(), testConfiguration.getPassword());
    }

    public void waitXSeconds(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<TapoDeviceKey> loginByIpAsync(String ipAddress, String email, String password) {
        CompletableFuture<TapoDeviceKey> future = client.loginByIpAsync(ipAddress, email, password);
        deviceKey = future.join();
        return future;
    }
}
