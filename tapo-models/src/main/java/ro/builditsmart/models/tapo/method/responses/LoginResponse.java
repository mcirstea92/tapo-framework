package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;

import java.util.Date;

@Data
public class LoginResponse {

    private final String sessionCookie;
    private final TapoDeviceProtocol deviceProtocol;
    @JsonIgnore
    private final String deviceIp;
    @JsonIgnore
    private final Long timeout;
    @JsonIgnore
    private final Date issueTime;
    @JsonIgnore
    private final Date expireTime;
    /**
     * This property will be the semaphore to indicate if this session is not expired
     */
    @JsonIgnore
    private boolean valid;

    public LoginResponse(TapoDeviceKey deviceKey) {
        this.deviceProtocol = deviceKey.getDeviceProtocol();
        this.deviceIp = deviceKey.getDeviceIp();
        this.sessionCookie = deviceKey.getSessionCookie();
        this.timeout = deviceKey.getTimeout().toSeconds();
        this.issueTime = deviceKey.getIssueTime();
        this.expireTime = new Date(this.issueTime.getTime() + deviceKey.getTimeout().toSeconds() * 1000L);
        this.valid = true;
    }

    public void invalidate() {
        this.valid = false;
    }

}
