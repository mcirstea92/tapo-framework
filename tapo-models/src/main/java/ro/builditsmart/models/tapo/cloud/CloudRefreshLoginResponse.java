package ro.builditsmart.models.tapo.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ro.builditsmart.models.tapo.method.responses.TapoResponse;

public class CloudRefreshLoginResponse extends TapoResponse<CloudRefreshLoginResponse.CloudRefreshLoginResult> {

    @Data
    public static class CloudRefreshLoginResult {

        @JsonProperty("token")
        private String token;

    }
}
