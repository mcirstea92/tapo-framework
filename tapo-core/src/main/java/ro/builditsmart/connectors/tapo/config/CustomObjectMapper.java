package ro.builditsmart.connectors.tapo.config;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        registerModule(new CustomDateModule());
    }

}
