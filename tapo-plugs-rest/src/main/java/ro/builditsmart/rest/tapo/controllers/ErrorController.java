package ro.builditsmart.rest.tapo.controllers;

import lombok.Builder;
import lombok.Data;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static ro.builditsmart.rest.tapo.utils.Constants.CORRELATION_ID;

@RestControllerAdvice
public class ErrorController {

    @Data
    @Builder
    public static class ErrorMessage {
        private String message;
        private String description;
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage anyException(Throwable ex, WebRequest request) {
        return ErrorMessage.builder()
                .message(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage())
                .description(String.format("Please contact us specifying the correlationId: `%s`", MDC.get(CORRELATION_ID)))
                .build();
    }

}
