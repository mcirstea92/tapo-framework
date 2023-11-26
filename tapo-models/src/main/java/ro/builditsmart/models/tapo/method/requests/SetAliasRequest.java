package ro.builditsmart.models.tapo.method.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SetAliasRequest {

    @JsonProperty("nickname")
    private String nickname;

}
