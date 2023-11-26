package ro.builditsmart.connectors.tapo.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public enum StaticBeans {
    ;

    private static final CustomObjectMapper jsonMapper;

    private static final OkHttpClient httpClient;

    static {
        jsonMapper = new CustomObjectMapper();
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        httpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public static ObjectMapper jsonMapper() {
        return jsonMapper;
    }

    public static OkHttpClient httpClient() {
        return httpClient;
    }

}
