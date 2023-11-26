package ro.builditsmart.models.tapo.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ro.builditsmart.models.tapo.method.responses.TapoResponse;

import java.util.Date;

public class CloudLoginResponse extends TapoResponse<CloudLoginResponse.CloudLoginResult> {

    @Data
    public static class CloudLoginResult {
        @JsonProperty("accountId")
        private String accountId;

        @JsonProperty("regTime")
        private Date regTime;

        @JsonProperty("countryCode")
        private String countryCode;

        @JsonProperty("riskDetected")
        private int riskDetected;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("email")
        private String email;

        @JsonProperty("token")
        private String token;

        @JsonProperty("refreshToken")
        private String refreshToken;

    }

}


