package ro.builditsmart.connectors.tapo;

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
import ro.builditsmart.connectors.tapo.config.properties.TestConfiguration;
import ro.builditsmart.connectors.tapo.protocol.TapoDeviceClient;
import ro.builditsmart.models.tapo.TapoSetDeviceState;
import ro.builditsmart.models.tapo.method.requests.TapoSetBulbState;
import ro.builditsmart.models.tapo.method.responses.StatusTapoResponse;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("klap-tests")
@SpringBootTest
@KlapTest
@Slf4j
class TapoDeviceClientTest extends BaseTest {

    @Autowired
    private TestConfiguration testConfiguration;

    public TapoDeviceClientTest() {
        super(new TapoDeviceClient());
    }

    @PostConstruct
    void postConstruct() {
        initialize(testConfiguration);
    }

    private static final boolean OFF_STATE = false;

    @Test
    @Order(1)
    void getDeviceInfoAsync() {
        var deviceInfo = client.getDeviceInfoAsync(deviceKey).join();
        log.info("Device info: {}", deviceInfo);
        assertNotNull(deviceInfo, "Device info should be not null");
    }

    @Test
    @Order(2)
    void setPowerAsync() {
        waitXSeconds(500);
        var powerInfo = client.setPowerAsync(deviceKey, OFF_STATE).join();
        log.info("Power info: {}", powerInfo);
        assertNotNull(powerInfo, "Power info should be not null");
    }

    @Test
    @Order(3)
    void pingDevice() {
        boolean isOnline = client.pingDevice(testDevice.getAddress());
        log.info("Device at '{}' is online: '{}'", testDevice.getAddress(), isOnline);
        assertTrue(isOnline, "isOnline should be true");
    }

    @Test
    @Order(4)
    void testSetStateAsync() {
        waitXSeconds(500);
        TapoSetDeviceState deviceState = new TapoSetBulbState(!OFF_STATE);
        StatusTapoResponse response = client.setStateAsync(deviceKey, deviceState).join();
        assertEquals(0, response.getErrorCode(), "The response code should be 0 - no error");
    }

    @Test
    @Order(5)
    void testGetDeviceEnergyUsageAsync() {
        waitXSeconds(500);
        CompletionException unexpectedError1002 = assertThrows(CompletionException.class,
                () -> client.getEnergyUsageAsync(deviceKey).join());
        assertEquals("Unexpected Error Code: 1002", unexpectedError1002.getCause().getMessage(), "The message of the exception should match");
    }

}