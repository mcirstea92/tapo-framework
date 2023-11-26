package ro.builditsmart.connectors.tapo.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class TapoCrypto {

    private static final String SHA1_ALGORITHM = "SHA1";
    private static final String SHA256_ALGORITHM = "SHA256";
    private static final String RSA = "RSA";
    public static final String CP1252 = "cp1252";

    public static String uuidV4() {
        return UUID.randomUUID().toString();
    }

    @SneakyThrows
    public static String base64Encode(String plainText) {
        Objects.requireNonNull(plainText, "plainText cannot be null");

        byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().encodeToString(plainTextBytes);
    }

    public static String base64Decode(String base64EncodedData, boolean doubleDecoding) {
        Objects.requireNonNull(base64EncodedData, "base64EncodedData cannot be null");
        byte[] base64DecodedBytes;
        String out = null;
        if (doubleDecoding) {
            try {
                base64EncodedData = Base64.getEncoder().encodeToString(base64EncodedData.getBytes(CP1252));
                base64DecodedBytes = Base64.getDecoder().decode(base64EncodedData);
                base64EncodedData = new String(base64DecodedBytes, StandardCharsets.UTF_8);
                out = new String(base64DecodedBytes, StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                log.warn("Could not encode with CP1252 charset");
                return base64EncodedData;
            }
        }
        try {
            base64DecodedBytes = Base64.getDecoder().decode(base64EncodedData);
            out = new String(base64DecodedBytes, StandardCharsets.UTF_8);
        } catch (Exception ignored) {

        }
        return out;
    }

    public static byte[] sha1Hash(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes cannot be null");
        try {
            return MessageDigest.getInstance(SHA1_ALGORITHM).digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    public static byte[] sha256Encode(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes cannot be null");
        try {
            return MessageDigest.getInstance(SHA256_ALGORITHM).digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static Cipher getCryptoAlgorithm(byte[] key, byte[] iv, int encryptMode) throws Exception {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(iv, "iv cannot be null");

        SecretKey secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(encryptMode, secretKey, ivParameterSpec);

        return cipher;
    }

    public static byte[] encrypt(String data, byte[] key, byte[] iv) throws Exception {
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(iv, "iv cannot be null");
        log.info("Encrypting {} with key {} and iv {}", data, key, iv);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = getCryptoAlgorithm(key, iv, Cipher.ENCRYPT_MODE);
        return cipher.doFinal(dataBytes);
    }

    public static byte[] decrypt(byte[] encryptedData, byte[] key, byte[] iv) throws Exception {
        Objects.requireNonNull(encryptedData, "encryptedData cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(iv, "iv cannot be null");
        Cipher cipher = getCryptoAlgorithm(key, iv, Cipher.DECRYPT_MODE);
        return cipher.doFinal(encryptedData);
    }

    public static byte[] generateRandomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be a positive value");
        }
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            byte[] randomByte = new byte[1];
            secureRandom.nextBytes(randomByte);
            while (randomByte[0] < 0) {
                secureRandom.nextBytes(randomByte);
            }
            randomBytes[i] = randomByte[0];
        }
        return randomBytes;
    }

    public static RSAKeyPair generateKeyPair(String password, int length) throws Exception {
        Objects.requireNonNull(password, "password cannot be null");
        Security.addProvider(new BouncyCastleProvider());
        SecureRandom random = new SecureRandom();
        // Generate an RSA key pair
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        generator.initialize(length, random);
        KeyPair keyPair = generator.generateKeyPair();
        // Extract the private key and public key
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        // Save the public key and private key as PEM files
        String publicKeyPem = saveKeyAsPEM(publicKey);
        String privateKeyPem = saveKeyAsPEM(privateKey);
        // String privateKeyPem = saveEncryptedKeyAsPEM(privateKey, password);
        return new RSAKeyPair(publicKeyPem, privateKeyPem);
    }

    private static String saveKeyAsPEM(Key key) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
        pemWriter.writeObject(key);
        pemWriter.close();
        return stringWriter.toString();
    }

    public static byte[] decryptWithPrivateKeyAndPassword(String base64Input, String privateKey, String password) throws Exception {
        Objects.requireNonNull(base64Input, "base64Input cannot be null");
        Objects.requireNonNull(privateKey, "privateKey cannot be null");
        Objects.requireNonNull(password, "password cannot be null");
        byte[] bytesToDecrypt = Base64.getDecoder().decode(base64Input);
        AsymmetricBlockCipher decryptEngine = new PKCS1Encoding(new RSAEngine());
        AsymmetricKeyParameter privateKeyParams = null;
        try (StringReader txtReader = new StringReader(privateKey)) {
            PEMParser pemParser = new PEMParser(txtReader);
            Object obj;
            while ((obj = pemParser.readObject()) != null) {
                if (obj instanceof PEMKeyPair) {
                    privateKeyParams = PrivateKeyFactory.createKey(((PEMKeyPair) obj).getPrivateKeyInfo());
                    break;
                }
            }
        }

        if (privateKeyParams != null) {
            decryptEngine.init(false, privateKeyParams);
            return decryptEngine.processBlock(bytesToDecrypt, 0, bytesToDecrypt.length);
        } else {
            throw new IllegalArgumentException("Invalid private key");
        }
    }

}
