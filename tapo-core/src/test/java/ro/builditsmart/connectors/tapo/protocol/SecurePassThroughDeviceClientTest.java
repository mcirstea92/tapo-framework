package ro.builditsmart.connectors.tapo.protocol;

import grouping.PassthroughTest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.builditsmart.connectors.tapo.BaseTest;
import ro.builditsmart.connectors.tapo.config.properties.TapoDeviceConfig;
import ro.builditsmart.connectors.tapo.config.properties.TestConfiguration;
import ro.builditsmart.models.tapo.method.requests.TapoSetBulbState;
import ro.builditsmart.models.tapo.TapoSetDeviceState;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("passthrough-tests")
@SpringBootTest
@PassthroughTest
@Slf4j
class SecurePassThroughDeviceClientTest extends BaseTest {

    @Autowired
    private TestConfiguration testConfiguration;

    private static boolean initialState;

    public SecurePassThroughDeviceClientTest() {
        super(new SecurePassThroughDeviceClient("password"));
    }

    @PostConstruct
    void postConstruct() {
        initialize(testConfiguration);
    }

    @Test
    @Order(1)
    void testCorrectConfiguration() {
        assertNotNull(testConfiguration, "Configuration should be not null");
        assertNotNull(testConfiguration.getDevices(), "Devices should be not null");
        assertTrue(testConfiguration.getDevices().size() > 0, "At least a device should be configured");
        TapoDeviceConfig deviceConfig = testConfiguration.getDevices().get(0);
        assertNotNull(deviceConfig.getName(), "Name of the first device should be not null");
        assertNotNull(deviceConfig.getModel(), "Model of the first device should be not null");
        assertNotNull(deviceConfig.getType(), "Type of the first device should be not null");
        assertNotNull(deviceConfig.getProtocol(), "Protocol of the first device should be not null");
    }

    @Test
    @Order(2)
    void getDeviceInfoAsync() {
        if (deviceKey != null) {
            var infoAsync = client.getDeviceInfoAsync(deviceKey).join();
            initialState = infoAsync.isDeviceOn();
            assertNotNull(infoAsync, "infoAsync should be not null");
            log.info("Got device nickname as {}", infoAsync.getNickname());
            log.info("Got device ssid as {}", infoAsync.getSsid());
            log.info("Got device id as {}", infoAsync.getDeviceId());
            log.info("Got device model as {}", infoAsync.getModel());
        } else {
            fail("deviceKey is null, you need to login first");
        }
    }

    @Test
    @Order(3)
    void getEnergyUsageAsync() {
        if (deviceKey != null) {
            var infoAsync = client.getEnergyUsageAsync(deviceKey).join();
            log.info("Got device energy usage as {}", infoAsync);
            assertNotNull(infoAsync, "deviceKey should be not null");
        } else {
            fail("deviceKey is null, you need to login first");
        }
    }

    @Test
    @Order(4)
    void pingDevice() {
        boolean isOnline = client.pingDevice(testDevice.getAddress());
        log.info("Device at {} is online: {}", testDevice.getAddress(), isOnline);
        assertTrue(isOnline, "isOnline should be true");
    }

    @Test
    @Order(5)
    void toggleDeviceFewTimes() {
        if (deviceKey != null) {
            TapoSetDeviceState deviceState = new TapoSetBulbState(false);
            client.setStateAsync(deviceKey, deviceState);

            waitXSeconds(500);
            deviceState.setDeviceOn(true);
            client.setStateAsync(deviceKey, deviceState);

            waitXSeconds(500);
            // restart using add_countdown_rule
            client.setStateWithDelay(deviceKey, true, 5);
            waitXSeconds(500);
            // shutdown now
            client.setPowerAsync(deviceKey, false);
        } else {
            fail("deviceKey is null, you need to login first");
        }
    }


    @Test
    @Order(100)
    void revertToInitialState() {
        if (deviceKey != null) {
            client.setPowerAsync(deviceKey, initialState);
            var infoAsync = client.getDeviceInfoAsync(deviceKey).join();
            assertTrue(initialState == infoAsync.isDeviceOn(), "Device state should be the same as initial state");
        }
    }

}