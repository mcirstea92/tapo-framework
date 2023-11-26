package ro.builditsmart.rest.tapo.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Constants {

    public static final String CORRELATION_ID = "correlationId";

    public static <T> ResponseEntity<T> unauthorizedResponseEntity() {
        return new ResponseEntity<T>(HttpStatus.UNAUTHORIZED);
    }

    public static <T> ResponseEntity<T> badRequestResponseEntity() {
        return new ResponseEntity<T>(HttpStatus.BAD_REQUEST);
    }

}
