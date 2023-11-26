package ro.builditsmart.connectors.tapo.util;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static ro.builditsmart.connectors.tapo.protocol.ITapoDeviceClient.TIMEOUT;
import static ro.builditsmart.connectors.tapo.protocol.ITapoDeviceClient.TP_SESSION_KEY;

class TapoUtilsTest {

    @Test
    void isTapoDevice() {
        boolean isTapo = TapoUtils.isTapoDevice("TAPO.SMARTPLUG");
        assertFalse(isTapo, "Should not be a valid tapo device");
        isTapo = TapoUtils.isTapoDevice(TapoUtils.TAPOLinkDeviceType);
        assertTrue(isTapo, "Now this should be a valid Tapo Device");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            TapoUtils.isTapoDevice(null);
        });
        assertEquals("deviceType cannot be null", exception.getMessage(), "Exception message should match");
    }

    @Test
    void needsDoubleDecoding() {
        /* for now only TAPOLinkDeviceType needs double decoding */
        boolean needsDoubleDecoding = TapoUtils.needsDoubleDecoding(TapoUtils.TAPOLinkDeviceType);
        assertTrue(needsDoubleDecoding, "TAPOLinkDeviceType really needs double decoding for Nickname and SSID in my region - for diacritics");
        needsDoubleDecoding = TapoUtils.needsDoubleDecoding(TapoUtils.TAPOLinkBulbDeviceType);
        assertFalse(needsDoubleDecoding, "Bulbs don't really need double decoding");
    }

    @Test
    void obtainSetCookieHeader() {
        String validTapoCookieHeader = TP_SESSION_KEY + "=iu21y412512;" + TIMEOUT + "=86400";
        List<String> setCookieHeadersList = List.of(validTapoCookieHeader);
        Map<String, String> map = TapoUtils.obtainSetCookieHeader(setCookieHeadersList);
        assertTrue(map.containsKey(TP_SESSION_KEY), "The resulting map should contain the " + TP_SESSION_KEY + " key");
        assertTrue(map.containsKey(TIMEOUT), "The resulting map should contain the " + TIMEOUT + " key");

        String invalidTapoCookieHeader = "cookie=iu21y412512;" + TIMEOUT + "=86400";
        setCookieHeadersList = List.of(invalidTapoCookieHeader);
        map = TapoUtils.obtainSetCookieHeader(setCookieHeadersList);
        assertFalse(map.containsKey(TP_SESSION_KEY), "The resulting map shouldn't contain the " + TP_SESSION_KEY + " key");
    }

    @Test
    void formatMacAddressTest() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            TapoUtils.formatMacAddress(null);
        });
        assertEquals("mac cannot be null", exception.getMessage(), "Exception message should match");
        String unformatted = "aabbccddeeff";
        String formatted = "aa-bb-cc-dd-ee-ff";
        String formattedMac = TapoUtils.formatMacAddress(unformatted);
        assertEquals(formatted, formattedMac, "Formatted MAC addresses should match");
    }


    /*@Test*/
    void tryGetIpAddressByMacAddress() {
        String routerMacAddress = "60-a4-b7-08-b3-84";
        String routerIp = "192.168.1.1";
        Optional<String> foundIp = TapoUtils.tryGetIpAddressByMacAddress(routerMacAddress);
        assertTrue(foundIp.isPresent(), "Found IP should be present");
        assertEquals(routerIp, foundIp.get(), "The IPs should match");

        String invalidMacAddress = " -a4-b7-08-b3-84";
        foundIp = TapoUtils.tryGetIpAddressByMacAddress(invalidMacAddress);
        assertFalse(foundIp.isPresent(), "Found IP should be not present");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TapoUtils.tryGetIpAddressByMacAddress(null));
        assertEquals("macAddress cannot be null", exception.getMessage());
    }

    @Test
    void truncateByteArray() {
        byte[] bytes = new byte[]{8, 6, 4, 2};
        byte[] expected = new byte[]{8, 6};
        byte[] truncated = TapoUtils.truncateByteArray(bytes, 0, 2);
        assertArrayEquals(expected, truncated, "Byte arrays should match");
        truncated = TapoUtils.truncateByteArray(bytes, 0, 5);
        assertArrayEquals(bytes, truncated, "Byte arrays should match");
    }

    @Test
    void concatBytes() {
        byte[] bytes1 = new byte[]{8, 6};
        byte[] bytes2 = new byte[]{7, 5};
        byte[] expected = new byte[]{8, 6, 7, 5};
        byte[] concat = TapoUtils.concatBytes(bytes1, bytes2);
        assertArrayEquals(expected, concat, "Byte arrays should match");
        /* sending a null byte[] will throw an error */
        Exception e = assertThrows(NullPointerException.class,
                () -> TapoUtils.concatBytes(bytes1, null));
        assertNotNull(e, "Exception should be not null");
        assertEquals(e.getMessage(), "Cannot read the array length because \"b\" is null");
    }

    @Test
    void byteToHex() {
    }

    @Test
    void encodeHexString() {
    }

    @Test
    void pingInvalidAddress(){
        TapoUtils.pingAddress("invalidAddress");
    }

    @Test
    void parseCertificate() throws Exception {
        X509Certificate certificate = TapoUtils.parseCertificate(TapoUtils.CA_STRING);
        assertNotNull(certificate, "Parsed certificate should be not null");

        X509TrustManager trustManager = TapoUtils.createX509TrustManager(certificate);
        assertNotNull(trustManager, "Trust manager should be not null");

        X509Certificate[] acceptedIssuers = trustManager.getAcceptedIssuers();
        assertEquals(0, acceptedIssuers.length, "The list of accepted issuers should be 0");

        SSLSocketFactory socketFactory = TapoUtils.createSslSocketFactory(certificate);
        assertNotNull(socketFactory, "Socket factory should be not null");
    }

}