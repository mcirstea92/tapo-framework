package ro.builditsmart.connectors.tapo.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TapoUtils {

    public static final String TAPOLinkDeviceType = "SMART.TAPOPLUG";
    public static final String TAPOLinkBulbDeviceType = "SMART.TAPOBULB";
    public static final String TAPOLinkIpCameraDeviceType = "SMART.IPCAMERA";

    public static boolean isTapoDevice(String deviceType) {
        if (deviceType == null) {
            throw new IllegalArgumentException("deviceType cannot be null");
        }

        return switch (deviceType.toUpperCase()) {
            case TAPOLinkDeviceType, TAPOLinkBulbDeviceType, TAPOLinkIpCameraDeviceType -> true;
            default -> false;
        };
    }

    public static boolean needsDoubleDecoding(String deviceType) {
        return deviceType.equalsIgnoreCase(TAPOLinkDeviceType);
    }

    public static Map<String, String> obtainSetCookieHeader(List<String> setCookieHeaders) {
        String setCookie = setCookieHeaders.get(0);
        String[] keyValuePairs = setCookie.split(";");
        return Arrays.stream(keyValuePairs)
                .map(keyValue -> keyValue.split("="))
                .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
    }

    public static String formatMacAddress(String text) {
        if (text == null) {
            throw new IllegalArgumentException("mac cannot be null");
        }

        if (text.length() == 12) {
            return text.substring(0, 2) + "-" + text.substring(2, 4) + "-" + text.substring(4, 6) +
                    "-" + text.substring(6, 8) + "-" + text.substring(8, 10) + "-" + text.substring(10, 12);
        } else {
            return text.toLowerCase();
        }
    }

    public static Optional<String> tryGetIpAddressByMacAddress(String macAddress) {
        String ipAddress = getIpAddressByMacAddress(macAddress);
        return Optional.ofNullable(ipAddress);
    }

    public static String getIpAddressByMacAddress(String macAddress) {
        if (macAddress == null) {
            throw new IllegalArgumentException("macAddress cannot be null");
        }
        try {
            Process process = new ProcessBuilder("arp", "-a").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String tidyMac = formatMacAddress(macAddress);
            while ((line = reader.readLine()) != null) {
                if (line.contains(tidyMac)) {
                    String[] lineParts = line.trim().split("\\s+");
                    if (lineParts.length > 0) {
                        return lineParts[0];
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Truncate Byte Array
     *
     * @param bytes     full byteArray
     * @param srcPos    start index
     * @param newLength new length of the array
     */
    public static byte[] truncateByteArray(byte[] bytes, int srcPos, int newLength) {
        if (bytes.length < newLength) {
            return bytes;
        } else {
            byte[] truncated = new byte[newLength];
            System.arraycopy(bytes, srcPos, truncated, 0, newLength);
            return truncated;
        }
    }

    /**
     * Concat Byte Arrays
     */
    public static byte[] concatBytes(byte[]... bytes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] b : bytes) {
            try {
                outputStream.write(b);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Could not concatenate byte arrays");
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * byte to hex-string
     */
    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    /**
     * byteArray to hex-string
     */
    public static String encodeHexString(byte[] byteArray) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    public static boolean pingAddress(String deviceIp) {
        String host = deviceIp;
        String port = "80";
        if (deviceIp.contains(":")) {
            String[] hostAndPort = deviceIp.split(":");
            host = hostAndPort[0];
            port = hostAndPort[1];
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isReachable(2000)) {
                try (Socket ignored = new Socket(address, Integer.parseInt(port))) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static final String CA_STRING = """
            -----BEGIN CERTIFICATE-----
            MIIDBzCCAe+gAwIBAgIQT5x0ma7QnINHCQvhnmzR9zANBgkqhkiG9w0BAQsFADAV
            MRMwEQYDVQQDEwp0cC1saW5rLUNBMCAXDTE4MDExOTA4Mjc1MloYDzIwNjgwMTE5
            MDgzNzUyWjAVMRMwEQYDVQQDEwp0cC1saW5rLUNBMIIBIjANBgkqhkiG9w0BAQEF
            AAOCAQ8AMIIBCgKCAQEAuGG8n5zEUN1j5wuvUz4pAIMurhKHbpfUUu+b2acFHKS6
            iU9hNJWvDyhXcihY5Wz6aq9m4D5SZcgW3k31YoNNtrztDjdg2qw7AaX85S99/G0B
            VbIXktrhs34OW19WA/haDwut3dFhLem+gCRRKUXcmuqchZc84dY7JFVfhPcJci4m
            sRjLCFNO0ho9OX+MZwfO4BLaeAqKVoAor6rf4BXVtO0xjYHDKO0fb3AWLLJ4EjGe
            q6YieqPiYlPFEqRm5PrvBXTm0IuQogygyVpK4LHr/K207ZLyV33DxLLbsUgSEJVn
            pZUv/WUujXjlIDgxIvyZZCYiXO3dle2/MEvpmZk6JQIDAQABo1EwTzALBgNVHQ8E
            BAMCAYYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUxu2iBRTsef5iNnsADVhM
            JDQWi6kwEAYJKwYBBAGCNxUBBAMCAQAwDQYJKoZIhvcNAQELBQADggEBAB52Majd
            +wo3cb5BsTo63z2Psbbyl4ACMUaw68NxUMy61Oihx3mcLzLJqiIZcKePiHskLqLJ
            F7QfT9TqjvizMjFJVgsLuVubUBXKBzqyN+3KKlQci0PO3mH+ObhyaE7BzV+qrS3P
            dVTgsCWFv8DkgLTRudSWxL7VwVoedc7lRz5EroGgJ33nRGCR0ngcW919tLTARDQO
            pULmzulcdWeZgG+0PLX0xjJQIjFEvbOxR1Z+gxMupBz0rWFokmWYrcga8eWiWzjQ
            Ia3/ASBVJ69srV77trWlfLumkChbXk9i64NXBKnce0Jmll0Y9OC1nMPqrbQKnzcn
            dSAA4fejD/qMQn0=
            -----END CERTIFICATE-----""";

    public static SSLSocketFactory createSslSocketFactory(X509Certificate certificate) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new X509TrustManager[]{createX509TrustManager(certificate)}, null);
        return sslContext.getSocketFactory();
    }

    public static X509TrustManager createX509TrustManager(X509Certificate certificate) {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @SneakyThrows
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                if (chain.length == 1) {
                    chain[0].verify(certificate.getPublicKey());
                }
            }
        };
    }

    @SneakyThrows
    public static X509Certificate parseCertificate(String originalCertificatePem) {
        String pemCertificate = originalCertificatePem.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\n", "");
        byte[] certBytes = Base64.getDecoder().decode(pemCertificate);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
    }


}
