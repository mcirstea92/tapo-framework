package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class DeviceGetUsageResponse extends TapoResponse<DeviceGetUsageResponse.DeviceGetUsageResult> {

    @Data
    @ToString
    public static class DeviceGetUsageResult {

        @JsonProperty("time_usage")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Usage time_usage;

        @JsonProperty("power_usage")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Usage power_usage;

        @JsonProperty("saved_power")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Usage saved_power;

    }

    private static class Usage {
        @JsonProperty("today")
        private int today;
        @JsonProperty("past7")
        private int past7;
        @JsonProperty("past30")
        private int past30;
    }

}
