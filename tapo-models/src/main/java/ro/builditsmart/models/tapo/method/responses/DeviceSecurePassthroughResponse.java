package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class DeviceSecurePassthroughResponse extends TapoResponse<DeviceSecurePassthroughResponse.DeviceSecurePassthroughResult> {

    @Data
    public static class DeviceSecurePassthroughResult {

        @JsonProperty("response")
        private String response;

    }

}
