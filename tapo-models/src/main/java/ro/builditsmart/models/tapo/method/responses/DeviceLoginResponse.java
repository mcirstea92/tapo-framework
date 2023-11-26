package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeviceLoginResponse extends TapoResponse<DeviceLoginResponse.DeviceLoginResult> {

    @Data
    public static class DeviceLoginResult {

        @JsonProperty("token")
        private String token;

    }

}
