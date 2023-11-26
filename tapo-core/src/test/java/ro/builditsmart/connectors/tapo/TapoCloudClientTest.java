package ro.builditsmart.connectors.tapo;

import grouping.CloudTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.builditsmart.connectors.tapo.config.properties.TestConfiguration;
import ro.builditsmart.connectors.tapo.protocol.TapoCloudClient;
import ro.builditsmart.models.tapo.cloud.CloudLoginResponse;
import ro.builditsmart.models.tapo.cloud.TapoDeviceDTO;
import ro.builditsmart.models.tapo.video.TapoVideoPageItem;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("cloud-tests")
@SpringBootTest
@CloudTest
@Slf4j
class TapoCloudClientTest {

    @Autowired
    private TestConfiguration cfg;

    private TapoCloudClient createClient() {
        return new TapoCloudClient(cfg.getCloud().getType(), cfg.getCloud().getBase(), cfg.getCloud().getCare());
    }

    private CloudLoginResponse.CloudLoginResult doLogin(TapoCloudClient client, String email, String password, boolean needsRefresh) {
        return client.loginAsync(email, password, needsRefresh).join();
    }

    @Test
    @Order(1)
    void loginAsync() {
        TapoCloudClient client = createClient();
        var login = doLogin(client, cfg.getEmail(), cfg.getPassword(), false);
        String cloudToken = login.getToken();
        assertNotNull(cloudToken);
        assertNull(login.getRefreshToken());
        log.debug("After Cloud login got nickname: {}, register time: {}", login.getNickname(), login.getRegTime());
    }

    @Test
    @Order(2)
    void refreshLoginAsync() {
        TapoCloudClient client = createClient();
        var login = doLogin(client, cfg.getEmail(), cfg.getPassword(), true);
        assertNotNull(login.getToken());
        assertNotNull(login.getRefreshToken());
        var refreshLogin = client.refreshLoginAsync(login.getRefreshToken()).join();
        assertNotNull(refreshLogin.getToken());
        log.debug("Got refresh token: {}", refreshLogin.getToken());
    }

    @Test
    @Order(3)
    void listDevicesAsync() {
        TapoCloudClient client = createClient();
        var login = doLogin(client, cfg.getEmail(), cfg.getPassword(), false);
        var response = client.listDevicesAsync(login.getToken()).join();
        assertNotNull(response.getDeviceList());
        log.info("Got devices list: {}", response.getDeviceList());
        for (TapoDeviceDTO device : response.getDeviceList()) {
            log.debug("Device #{}_{}: \t\t {} ++++ {}", device.getDeviceModel(), device.getAlias(), device.getStatus(), device);
        }
    }

    @Order(4)
    void listVideos() {
        TapoCloudClient client = createClient();
        var login = doLogin(client, cfg.getEmail(), cfg.getPassword(), false);
        var response = client.listVideosAsync(login.getToken()).join();
        assertNotNull(response);
        log.info("Got devices list: {}", response.getIndex());
        for (TapoVideoPageItem pageItem : response.getIndex()) {
            log.debug("TapoVideoPageItem #{}_{}: \t\t {} ++++ {}", pageItem.getUuid(), pageItem.getCreatedTime(), pageItem.getEventLocalTime(), pageItem);
        }
    }

}