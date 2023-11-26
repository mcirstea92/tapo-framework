package ro.builditsmart.models.tapo.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TapoDeviceDTO {

    @JsonProperty("deviceType")
    private String deviceType;

    @JsonProperty("role")
    private int role;

    @JsonProperty("fwVer")
    private String fwVer;

    @JsonProperty("appServerUrl")
    private String appServerUrl;

    @JsonProperty("deviceRegion")
    private String deviceRegion;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("deviceName")
    private String deviceName;

    @JsonProperty("deviceHwVer")
    private String deviceHwVer;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("deviceMac")
    private String deviceMac;

    @JsonProperty("oemId")
    private String oemId;

    @JsonProperty("deviceModel")
    private String deviceModel;

    @JsonProperty("hwId")
    private String hwId;

    @JsonProperty("fwId")
    private String fwId;

    @JsonProperty("isSameRegion")
    private boolean isSameRegion;

    @JsonProperty("status")
    private int status;

}
