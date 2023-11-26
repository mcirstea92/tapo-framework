package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceGetLedInfoResponse extends TapoResponse<DeviceGetLedInfoResponse.DeviceGetLedInfoResult> {

    @Data
    public static class DeviceGetLedInfoResult {

        @JsonProperty("led_rule")
        private String ledRule;

        @JsonProperty("led_status")
        private Boolean ledStatus;

        @JsonProperty("night_mode")
        private NightMode nightMode;

    }

    @Data
    private static class NightMode {
        @JsonProperty("end_time")
        private Integer endTime;
        @JsonProperty("night_mode_type")
        private String nightModeType;
        @JsonProperty("start_time")
        private Integer startTime;
        @JsonProperty("sunrise_offset")
        private Integer sunriseOffset;
        @JsonProperty("sunset_offset")
        private Integer sunsetOffset;

    }

}
