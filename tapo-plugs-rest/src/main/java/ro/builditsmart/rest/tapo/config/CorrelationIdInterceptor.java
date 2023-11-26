package ro.builditsmart.rest.tapo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

import static ro.builditsmart.rest.tapo.utils.Constants.CORRELATION_ID;

public class CorrelationIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        MDC.remove(CORRELATION_ID);
    }
}
