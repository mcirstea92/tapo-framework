package ro.builditsmart.connectors.tapo.protocol;

import grouping.KlapTest;
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

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("klap-tests")
@SpringBootTest
@KlapTest
@Slf4j
class KlapDeviceClientTest extends BaseTest {

    @Autowired
    private TestConfiguration testConfiguration;

    private static boolean initialState;

    public KlapDeviceClientTest() {
        super(new KlapDeviceClient());
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
        var infoAsync = client.getDeviceInfoAsync(deviceKey).join();
        initialState = infoAsync.isDeviceOn();
        log.info("Got device nickname using klap protocol: {}", infoAsync.getNickname());
        log.info("Got device initial state using klap protocol: {}", initialState);
    }

    @Test
    @Order(3)
    void setDeviceStateWithDelayAsync() {
        var infoAsync = client.setStateWithDelay(deviceKey, true, 1).join();
        log.info("Got device info after setting device state using klap protocol: {}", infoAsync);
        waitXSeconds(500);
    }

    @Test
    @Order(4)
    void testRestartDevice() {
        waitXSeconds(2000);
        log.debug("Setting device state to ON after 2 seconds");
        var startAfter5SecondsResponse = client.setStateWithDelay(deviceKey, true, 3).join();
        log.debug("Got device info after setting timed device state: {}", startAfter5SecondsResponse);
        waitXSeconds(500);
        var shutdownNowInfoResponse = client.setPowerAsync(deviceKey, false).join();
        log.debug("Shut down device. Response: {}", shutdownNowInfoResponse);
        var infoAsync = client.getDeviceInfoAsync(deviceKey).join();
        assertFalse(infoAsync.isDeviceOn(), "Device should be turned off");
        waitXSeconds(3000);
        infoAsync = client.getDeviceInfoAsync(deviceKey).join();
        assertTrue(infoAsync.isDeviceOn(), "Device should be turned on");
    }

    @Test
    @Order(5)
    void shutdownDevice() {
        waitXSeconds(500);
        log.debug("Setting device state to OFF now");
        var shutdownInfoResponse = client.setPowerAsync(deviceKey, false).join();
        log.debug("Shut down device. Response: {}", shutdownInfoResponse);
    }

    @Test
    @Order(6)
    void energyStatus() {
        CompletionException exception = assertThrows(CompletionException.class, () -> client.getEnergyUsageAsync(deviceKey).join());
        assertEquals("Unexpected Error Code: 1002", exception.getCause().getCause().getMessage());
    }

    @Test
    @Order(100)
    void revertToInitialState() {
        waitXSeconds(500);
        log.info("Setting device state to initial state: {}", initialState);
        var infoResponse = client.setPowerAsync(deviceKey, initialState).join();
        log.debug("Device set to initial state. Response: {}", infoResponse);
    }

    @Test
    @Order(7)
    void getDeviceRunningInfoAsync() {
        var infoAsync = client.getDeviceRunningInfoAsync(deviceKey).join();
        log.debug("Got device running info using klap protocol: {}", infoAsync);
    }

    @Test
    @Order(8)
    void getDeviceUsageAsync() {
        var infoAsync = client.getDeviceUsageAsync(deviceKey).join();
        log.debug("Got device usage using klap protocol: {}", infoAsync);
    }

    @Test
    @Order(9)
    void getLedInfoAsync() {
        var infoAsync = client.getLedInfoAsync(deviceKey).join();
        log.debug("Got led info using klap protocol: {}", infoAsync);
    }

}