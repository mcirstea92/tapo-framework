package ro.builditsmart.connectors.tapo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomDateModuleTest {

    @Data
    private static class ToMapClass {
        Date date;
        String name;
    }

    @Test
    public void testInvalidDateFormat() {
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> {
            String invalidDate = "{ \"name\": \"Test Name\", \"date\": \"202 3-21-18 22:99:85\"}";
            CustomObjectMapper mapper = new CustomObjectMapper();
            mapper.readValue(invalidDate, ToMapClass.class);
        });
        assertEquals(exception.getMessage(), "Unexpected IOException (of type java.io.IOException): Error parsing date");
    }

}