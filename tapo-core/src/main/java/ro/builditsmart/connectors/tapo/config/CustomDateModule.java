package ro.builditsmart.connectors.tapo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CustomDateModule extends SimpleModule {

    public CustomDateModule() {
        addDeserializer(Date.class, new CustomDateDeserializer());
    }

    private static class CustomDateDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            String dateValue = node.asText();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                return sdf.parse(dateValue);
            } catch (Exception e) {
                throw new IOException("Error parsing date", e);
            }
        }
    }
}
