package ro.builditsmart.models.tapo.method.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString
@NoArgsConstructor
@SuperBuilder
public class CountdownRuleDTO {

    @JsonProperty("delay")
    private Integer delay;

    @JsonProperty("desired_states")
    private DesiredStates desiredStates;

    @JsonProperty("enable")
    private Boolean enable;

    @JsonProperty("remain")
    private Integer remain;

    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @ToString
    @Data
    @SuperBuilder
    @NoArgsConstructor
    public static class DesiredStates {

        @JsonProperty("on")
        private Boolean on;

    }

}
