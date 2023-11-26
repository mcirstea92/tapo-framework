package ro.builditsmart.connectors.tapo.protocol;

import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;

import java.time.Duration;
import java.util.Date;

public class SecurePassThroughDeviceKey extends TapoDeviceKey {

    private final byte[] key;
    private final byte[] iv;
    private final String token;

    public SecurePassThroughDeviceKey(
            String deviceIp,
            String sessionCookie,
            Duration timeout,
            Date issueTime,
            byte[] key,
            byte[] iv,
            String token) {
        super(TapoDeviceProtocol.SecurePassThrough, deviceIp, sessionCookie, timeout, issueTime);
        this.key = key;
        this.iv = iv;
        this.token = token;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getIv() {
        return iv;
    }

    public String getToken() {
        return token;
    }
}
