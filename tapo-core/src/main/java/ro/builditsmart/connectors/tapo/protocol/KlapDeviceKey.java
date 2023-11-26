package ro.builditsmart.connectors.tapo.protocol;

import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;

import java.time.Duration;
import java.util.Date;

public class KlapDeviceKey extends TapoDeviceKey {

    private final KlapCipher klapCipher;

    public KlapDeviceKey(String deviceIp, String sessionCookie, Duration timeout, Date issueTime, KlapCipher klapCipher) {
        super(TapoDeviceProtocol.Klap, deviceIp, sessionCookie, timeout, issueTime);
        this.klapCipher = klapCipher;
    }

    public KlapCipher getKlapCipher() {
        return klapCipher;
    }

}
