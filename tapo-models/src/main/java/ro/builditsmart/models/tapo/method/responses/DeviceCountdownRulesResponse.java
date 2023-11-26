package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ro.builditsmart.models.tapo.method.requests.CountdownRuleDTO;

import java.util.List;

@Data
public class DeviceCountdownRulesResponse extends TapoResponse<DeviceCountdownRulesResponse.DeviceCountdownRulesResult> {

    @Data
    public static class DeviceCountdownRulesResult {
        @JsonProperty("enable")
        private Boolean enable;

        @JsonProperty("countdown_rule_max_count")
        private Integer countdownRuleMaxCount;

        @JsonProperty("rule_list")
        private List<CountdownRuleDTO> ruleList;
    }

}
