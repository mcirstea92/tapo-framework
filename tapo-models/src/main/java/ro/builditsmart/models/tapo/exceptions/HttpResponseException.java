package ro.builditsmart.models.tapo.exceptions;

import java.util.Objects;

public class HttpResponseException extends TapoException {

    private final int statusCode;

    public HttpResponseException(int statusCode, String body) {
        super(statusCode + ": " + body);
        this.statusCode = statusCode;
        Objects.requireNonNull(body);
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}
