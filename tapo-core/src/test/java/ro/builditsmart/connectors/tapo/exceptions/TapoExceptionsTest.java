package ro.builditsmart.connectors.tapo.exceptions;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import ro.builditsmart.models.tapo.exceptions.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TapoExceptionsTest {

    @Test
    public void testHttpResponseException() {
        Response response = new Response.Builder()
                .code(429)
                .request(new Request.Builder().url("https://www.example.org").build())
                .protocol(Protocol.HTTP_1_0)
                .message("No message")
                .body(ResponseBody.create("some response body", MediaType.parse("text/plain")))
                .build();
        HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> {
                    String body = Objects.requireNonNull(response.body()).string();
                    throw new HttpResponseException(response.code(), body);
                });
        assertEquals(429, exception.getStatusCode(), "Status codes should match");
    }

    @Test
    public void testCloudTokenExpiredOrInvalidException() {
        TapoException exception = assertThrows(TapoCloudTokenExpiredOrInvalidException.class,
                () -> {
                    throw new TapoCloudTokenExpiredOrInvalidException("Invalid token: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testDeviceTokenExpiredOrInvalidException() {
        TapoException exception = assertThrows(TapoDeviceTokenExpiredOrInvalidException.class,
                () -> {
                    throw new TapoDeviceTokenExpiredOrInvalidException("Invalid token: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testInvalidRequestException() {
        TapoException exception = assertThrows(TapoInvalidRequestException.class,
                () -> {
                    throw new TapoInvalidRequestException(400, "Invalid request exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testJsonException() {
        TapoException exception = assertThrows(TapoJsonException.class,
                () -> {
                    throw new TapoJsonException("Invalid json exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testKlapException() {
        TapoException exception = assertThrows(TapoKlapException.class,
                () -> {
                    throw new TapoKlapException("Invalid klap exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testNoSetCookieHeaderException() {
        TapoException exception = assertThrows(TapoNoSetCookieHeaderException.class,
                () -> {
                    throw new TapoNoSetCookieHeaderException("NoSetCookie Header exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testProtocolDeprecatedException() {
        TapoException exception = assertThrows(TapoProtocolDeprecatedException.class,
                () -> {
                    throw new TapoProtocolDeprecatedException("Protocol deprecated exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testProtocolMismatchException() {
        TapoException exception = assertThrows(TapoProtocolMismatchException.class,
                () -> {
                    throw new TapoProtocolMismatchException("Protocol mismatch exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testSecurePassThroughException() {
        TapoException exception = assertThrows(TapoSecurePassThroughProtocolDeprecatedException.class,
                () -> {
                    throw new TapoSecurePassThroughProtocolDeprecatedException("SecurePassThrough exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testTokenExpiredException() {
        TapoException exception = assertThrows(TapoTokenExpiredException.class,
                () -> {
                    throw new TapoTokenExpiredException("Token expired exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }


    @Test
    public void testUnknownDeviceKeyProtocolException() {
        TapoException exception = assertThrows(TapoUnknownDeviceKeyProtocolException.class,
                () -> {
                    throw new TapoUnknownDeviceKeyProtocolException("Unknown device key protocol exception: -------");
                });
        assertNotNull(exception, "Exception should be not null");
    }

    @Test
    public void testTapoExceptions() {
        Exception exception = assertThrows(TapoInvalidRequestException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.RequestMethodNotSupportedErrorCode);
                });
        assertEquals("Request method not supported", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoInvalidRequestException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.InvalidPublicKeyLengthErrorCode);
                });
        assertEquals("Invalid public key length", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoInvalidRequestException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.InvalidRequestOrCredentialsErrorCode);
                });
        assertEquals("Invalid request or credentials", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoInvalidRequestException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.ParameterDoesntExistErrorCode);
                });
        assertEquals("Parameter doesn't exist", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoInvalidRequestException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.IncorrectRequestErrorCode);
                });
        assertEquals("Incorrect request", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoJsonException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.JsonFormatErrorCode);
                });
        assertEquals("JSON format error", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoCloudTokenExpiredOrInvalidException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.CloudTokenExpiredOrInvalidErrorCode);
                });
        assertEquals("Cloud token expired or invalid", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoDeviceTokenExpiredOrInvalidException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.DeviceTokenExpiredOrInvalidErrorCode);
                });
        assertEquals("Device token expired or invalid", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoTokenExpiredException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.TokenExpiredErrorCode);
                });
        assertEquals("Token expired", exception.getMessage(), "The exception message should match");
        exception = assertThrows(TapoSecurePassThroughProtocolDeprecatedException.class,
                () -> {
                    TapoException.throwFromErrorCode(TapoException.SecurePassthroughDeprecated);
                });
        assertEquals("Secure passThrough protocol deprecated in firmware >= \"1.1.0 Build 230721 Rel.224802\" for KLAP.", exception.getMessage(), "The exception message should match");
        int code = 12345;
        exception = assertThrows(TapoException.class,
                () -> {
                    TapoException.throwFromErrorCode(code);
                });
        assertEquals("Unexpected Error Code: " + code, exception.getMessage(), "The exception message should match");
    }

}