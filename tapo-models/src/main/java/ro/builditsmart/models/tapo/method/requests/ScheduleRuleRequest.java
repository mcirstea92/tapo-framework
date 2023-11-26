package ro.builditsmart.models.tapo.method.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ScheduleRuleRequest {

    @JsonProperty("start_index")
    private int startIndex;

}
