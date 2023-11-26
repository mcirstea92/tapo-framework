package ro.builditsmart.models.tapo.method.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TapoDelayedRequest {

    @JsonProperty("seconds_delay")
    private Integer seconds_delay;

    @JsonProperty("state")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean state;

}
