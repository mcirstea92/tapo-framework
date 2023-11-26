package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class DeviceGetInfoResponse extends TapoResponse<DeviceGetInfoResponse.DeviceGetInfoResult> {

    @Data
    @ToString
    public static class DeviceGetInfoResult {

        @JsonProperty("auto_off_remain_time")
        private Long autoOffRemainTime;

        @JsonProperty("auto_off_status")
        private String autoOffStatus;

        @JsonProperty("avatar")
        private String avatar;

        @JsonProperty("default_states")
        private DeviceGetInfoDefaultStateDto defaultState;

        @JsonProperty("device_id")
        private String deviceId;

        @JsonProperty("device_on")
        private boolean deviceOn;

        @JsonProperty("fw_id")
        private String fwId;

        @JsonProperty("fw_ver")
        private String fwVersion;

        @JsonProperty("has_set_location_info")
        private boolean hasSetLocationInfo;

        @JsonProperty("hw_id")
        private String hwId;

        @JsonProperty("hw_ver")
        private String hwVersion;

        @JsonProperty("ip")
        private String ip;

        @JsonProperty("lang")
        private String lang;

        @JsonProperty("latitude")
        private int latitude;

        @JsonProperty("longitude")
        private int longitude;

        @JsonProperty("mac")
        private String mac;

        @JsonProperty("model")
        private String model;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("oem_id")
        private String oemId;

        @JsonProperty("on_time")
        private Long onTime;

        @JsonProperty("overheated")
        private boolean overheated;

        @JsonProperty("power_protection_status")
        private String powerProtectionStatus;

        @JsonProperty("region")
        private String region;

        @JsonProperty("rssi")
        private int rssi;

        @JsonProperty("signal_level")
        private int signalLevel;

        @JsonProperty("specs")
        private String specs;

        @JsonProperty("ssid")
        private String ssid;

        @JsonProperty("time_diff")
        private int timeDiff;

        @JsonProperty("type")
        private String type;

        /**
         * tapo bulbs related
         */
        @JsonProperty("brightness")
        private int brightness;

        @JsonProperty("color_temp")
        private int colorTemperature;

        @JsonProperty("color_temp_range")
        private List<Integer> colorTemperatureRange;

        @JsonProperty("dynamic_light_effect_enable")
        private boolean dynamicLightEffectEnable;

        @JsonProperty("hue")
        private int hue;

        @JsonProperty("saturation")
        private int saturation;

    }

    @Data
    public static class DeviceGetInfoDefaultStateDto {

        @JsonProperty("type")
        private String type;

        @JsonProperty("state")
        private JsonNode state;

    }
}
