package ro.builditsmart.connectors.tapo.util;

import grouping.GeneralTest;
import grouping.PassthroughTest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.builditsmart.connectors.tapo.config.properties.TestConfiguration;
import ro.builditsmart.connectors.tapo.protocol.SecurePassThroughDeviceClient;
import ro.builditsmart.models.tapo.method.responses.DeviceHandshakeResponse;
import ro.builditsmart.models.tapo.exceptions.TapoException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@GeneralTest
@ActiveProfiles("passthrough-tests")
@SpringBootTest
class TapoCryptoTest {

    @Autowired
    private TestConfiguration cfg;

    @Test
    void uuidV4() {
        var uuid = TapoCrypto.uuidV4();
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
    }

    @Test
    void base64Encode() {
        String unEncoded = "Smart PC";
        String encoded = TapoCrypto.base64Encode(unEncoded);
        String encodedNickname = "U21hcnQgUEM=";
        assertEquals(encodedNickname, encoded, "The encoded strings should match");
    }

    @Test
    void base64Decode() {
        String unEncoded = "Smart PC";
        String deviceType = TapoUtils.TAPOLinkDeviceType;
        String encodedNickname = "U21hcnQgUEM=";
        String decoded = TapoCrypto.base64Decode(encodedNickname, TapoUtils.needsDoubleDecoding(deviceType));
        assertEquals(unEncoded, decoded, "The decoded strings should match");
    }

    @Test
    void testEncodingDecode() {
        String unEncoded = "Special encoding example";
        String deviceType = TapoUtils.TAPOLinkDeviceType;
        String encoded = TapoCrypto.base64Encode(unEncoded);
        String encodedNickname = "U3BlY2lhbCBlbmNvZGluZyBleGFtcGxl";
        assertEquals(encodedNickname, encoded, "The encoded strings should match");
        String decoded = TapoCrypto.base64Decode(encodedNickname, TapoUtils.needsDoubleDecoding(deviceType));
        assertEquals(unEncoded, decoded, "The decoded strings should match");

        byte[] specialCharsBytes = "ȚĂRUȘ".getBytes(StandardCharsets.UTF_8);
        String specialChars = new String(specialCharsBytes, StandardCharsets.UTF_8);
        encoded = TapoCrypto.base64Encode(specialChars);
        encodedNickname = "yJrEglJVyJg=";
        assertEquals(encodedNickname, encoded, "The encoded strings should match");
        decoded = TapoCrypto.base64Decode(encodedNickname, TapoUtils.needsDoubleDecoding(deviceType));
        assertEquals(specialChars, decoded, "The decoded strings should match");
    }

    @Test
    void sha1Hash() {
        byte[] bytes = "some-string".getBytes(StandardCharsets.UTF_8);
        byte[] sha1 = TapoCrypto.sha1Hash(bytes);

        assertNotNull(sha1, "SHA-1 hash should not be null");
        assertEquals(20, sha1.length, "SHA-1 hash should be 20 bytes in length");

        byte[] expectedHash = new byte[]{2, -122, 76, -100, -97, -104, 98, 108, 15, 112, -100, -116, 16, -53, -6, -2, 38, 46, 39, -120};
        assertArrayEquals(expectedHash, sha1, "SHA-1 hash should match the expected hash");
    }

    @Test
    void sha256Hash() {
        byte[] bytes = "some-string".getBytes(StandardCharsets.UTF_8);
        byte[] sha256 = TapoCrypto.sha256Encode(bytes);

        assertNotNull(sha256, "SHA-256 hash should not be null");
        assertEquals(32, sha256.length, "SHA-256 hash should be 32 bytes in length");

        byte[] expectedHash = new byte[]{-93, 99, 92, 9, -67, -89, 41, 58, -31, -15, 68, -94, 64, -15, 85, -49, 21, 20, 81, -14, 66, 13, 17, -84, 56, 93, 19, -52, -28, -21, 95, -94};
        assertArrayEquals(expectedHash, sha256, "SHA-256 hash should match the expected hash");
    }

    @Test
    void encrypt() {
        byte[] key = new byte[]{-93, 99, 92, 9, -67, -89, 41, 58, -31, -15, 68, -94, 64, -15, 85, -49};
        byte[] iv = new byte[]{21, 20, 81, -14, 66, 13, 17, -84, 56, 93, 19, -52, -28, -21, 95, -94};
        String someString = "some-string";
        byte[] encrypted;
        try {
            encrypted = TapoCrypto.encrypt(someString, key, iv);
        } catch (Exception e) {
            fail("Failed to encrypt string", e);
            return;
        }
        assertNotNull(encrypted, "Encrypted byte array should not be null");
        assertEquals(16, encrypted.length, "Encrypted byte array should contain 16 chars");
        assertArrayEquals(new byte[]{86, 48, -58, -35, -38, -11, 45, 108, -78, 88, 26, -56, -46, -16, 25, 71}, encrypted, "Byte arrays contents should match");
    }

    @Test
    void decrypt() {
        byte[] encryptedBytes = new byte[]{86, 48, -58, -35, -38, -11, 45, 108, -78, 88, 26, -56, -46, -16, 25, 71};
        byte[] key = new byte[]{-93, 99, 92, 9, -67, -89, 41, 58, -31, -15, 68, -94, 64, -15, 85, -49};
        byte[] iv = new byte[]{21, 20, 81, -14, 66, 13, 17, -84, 56, 93, 19, -52, -28, -21, 95, -94};
        String someString = "some-string";
        byte[] decrypted;
        try {
            decrypted = TapoCrypto.decrypt(encryptedBytes, key, iv);
        } catch (Exception e) {
            fail("Failed to decrypted bytes into string", e);
            return;
        }
        assertNotNull(decrypted, "Decrypted byte array should not be null");
        assertEquals(11, decrypted.length, "Decrypted byte array should contain 16 chars");
        assertArrayEquals(new byte[]{115, 111, 109, 101, 45, 115, 116, 114, 105, 110, 103}, decrypted, "Byte arrays contents should match");
        assertEquals(someString, new String(decrypted, StandardCharsets.UTF_8), "Strings should match after decryption");
    }

    @Test
    void generateRandomBytes() {
        byte[] randomBytes = TapoCrypto.generateRandomBytes(16);
        assertEquals(16, randomBytes.length);
    }

    @Test
    @PassthroughTest
    void testDecryptWithPrivateKeyAndPassword() {
        String password = "ourCustomPassword";
        RSAKeyPair pair;
        try {
            pair = TapoCrypto.generateKeyPair(password, 1024);
        } catch (Exception e) {
            fail("Could not generate an RSA keypair", e);
            return;
        }
        String pemPrivate = pair.getPrivateKey();

        SecurePassThroughDeviceClient client = new SecurePassThroughDeviceClient();
        Map<DeviceHandshakeResponse, Headers> map = client.makeHandshake(cfg.getDevices().get(0).getAddress(), pair);

        DeviceHandshakeResponse key;
        try {
            key = map.keySet().stream()
                    .findFirst()
                    .orElseThrow(() -> new TapoException("No device handshake response could be found!"));
        } catch (TapoException e) {
            fail("Could not realise a valid device handshake", e);
            return;
        }
        DeviceHandshakeResponse.DeviceHandshakeResult result = key.getResult();

        SecurePassThroughDeviceClient.TapoHandshakeKey tapoHandshakeKey = client.createHandshakeKey(map, pair);
        assertNotNull(tapoHandshakeKey, "tapoHandshakeKey should not be null");
        String resultKey = result.getKey();

        byte[] deviceKey;
        try {
            deviceKey = TapoCrypto.decryptWithPrivateKeyAndPassword(resultKey, pemPrivate, password);
        } catch (Exception e) {
            fail("Failed to decrypt deviceKey: " + e.getMessage());
            return;
        }

        byte[] keyArray = Arrays.copyOf(deviceKey, 16);
        byte[] ivArray = Arrays.copyOfRange(deviceKey, 16, 32);

        assertArrayEquals(tapoHandshakeKey.getKey(), keyArray, "key bytes should match");
        assertArrayEquals(tapoHandshakeKey.getIv(), ivArray, "iv bytes should match");
    }

}