package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@ToString
public class DeviceEnergyInfoResponse extends TapoResponse<DeviceEnergyInfoResponse.DeviceEnergyInfoResult> {

    @Data
    @ToString
    public static class DeviceEnergyInfoResult {

        @JsonProperty("today_runtime")
        private Integer todayRuntime;

        @JsonProperty("month_runtime")
        private Integer monthRuntime;

        @JsonProperty("today_energy")
        private Integer todayEnergy;

        @JsonProperty("month_energy")
        private Integer monthEnergy;

        @JsonProperty("local_time")
        private Date localTime;

        @JsonProperty("electricity_charge")
        private List<Integer> electricityCharge;

        @JsonProperty("current_power")
        private Integer currentPower;

    }

}
