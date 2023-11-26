package ro.builditsmart.models.tapo;

import lombok.Data;
import lombok.ToString;
import ro.builditsmart.models.tapo.exceptions.TapoProtocolMismatchException;

import java.time.Duration;
import java.util.Date;

@ToString
@Data
public abstract class TapoDeviceKey {

    private final TapoDeviceProtocol deviceProtocol;
    private final String deviceIp;
    private final String sessionCookie;
    private final Duration timeout;
    private final Date issueTime;

    public TapoDeviceKey(
            TapoDeviceProtocol deviceProtocol,
            String deviceIp,
            String sessionCookie,
            Duration timeout,
            Date issueTime) {
        this.deviceProtocol = deviceProtocol;
        this.deviceIp = deviceIp;
        this.sessionCookie = sessionCookie;
        this.timeout = timeout;
        this.issueTime = issueTime;
    }

    public TapoDeviceProtocol getDeviceProtocol() {
        return deviceProtocol;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public <TProtocol extends TapoDeviceKey> TProtocol toProtocol(Class<TProtocol> protocolClass) throws TapoProtocolMismatchException {
        if (protocolClass.isInstance(this)) {
            return protocolClass.cast(this);
        } else {
            throw new TapoProtocolMismatchException("Protocol " + getClass().getName() +
                    " cannot be converted to " + protocolClass.getName() + ".");
        }
    }

}