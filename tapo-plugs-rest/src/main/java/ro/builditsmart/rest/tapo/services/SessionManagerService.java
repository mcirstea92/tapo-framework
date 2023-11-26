package ro.builditsmart.rest.tapo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ro.builditsmart.models.tapo.TapoDeviceKey;
import ro.builditsmart.models.tapo.method.responses.LoginResponse;
import ro.builditsmart.rest.tapo.utils.KeyValue;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SessionManagerService {

    private final Map<String, KeyValue<TapoDeviceKey, LoginResponse>> sessionCache = new ConcurrentHashMap<>();

    public void addSessionInfo(String sessionId, TapoDeviceKey deviceKey, LoginResponse sessionInfo) {
        this.sessionCache.put(sessionId, new KeyValue<>(deviceKey, sessionInfo));
        log.debug("Session info added for sessionId {} as {}", sessionId, sessionInfo);
    }

    public Optional<KeyValue<TapoDeviceKey, LoginResponse>> getKeyPair(String sessionId) {
        return Optional.ofNullable(this.sessionCache.get(sessionId));
    }

    @Scheduled(cron = "${tplink.config.cron.cache.invalidate:0 */1 * * * *}")
    private void invalidateExpiredSessions() {
        log.debug("Invalidating expired sessions");
        Date now = new Date(System.currentTimeMillis());
        for (var session : sessionCache.values()) {
            if (session.getValue().getExpireTime().before(now)) {
                session.getValue().invalidate();
            }
        }
    }

    @Scheduled(cron = "${tplink.config.cron.cache.cleanup:0 */5 * * * *}")
    private void cleanupExpiredSessions() {
        for (var session : sessionCache.values()) {
            if (!session.getValue().isValid()) {
                log.debug("Removing session with id {}", session.getValue().getSessionCookie());
                sessionCache.remove(session.getValue().getSessionCookie());
            }
        }
        log.debug("Active sessions remaining: {}", this.sessionCache.size());
    }

    public boolean invalidateSession(String sessionId) {
        log.debug("Trying to invalidate session with id {}", sessionId);
        if (!sessionCache.containsKey(sessionId)) {
            return false;
        }
        var wasValid = this.sessionCache.get(sessionId).getValue().isValid();
        this.sessionCache.get(sessionId).getValue().invalidate();
        return wasValid;
    }

}
