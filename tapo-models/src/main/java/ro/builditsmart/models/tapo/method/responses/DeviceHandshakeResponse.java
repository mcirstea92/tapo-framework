package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class DeviceHandshakeResponse extends TapoResponse<DeviceHandshakeResponse.DeviceHandshakeResult> {

    @Data
    public static class DeviceHandshakeResult {

        @JsonProperty("key")
        private String key;
    }

}
