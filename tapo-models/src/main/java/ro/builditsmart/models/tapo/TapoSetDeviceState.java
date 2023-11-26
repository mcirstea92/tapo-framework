package ro.builditsmart.models.tapo;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class TapoSetDeviceState  {

    @JsonProperty("device_on")
    private Boolean deviceOn;

    public Boolean getDeviceOn() {
        return deviceOn;
    }

    public void setDeviceOn(Boolean deviceOn) {
        this.deviceOn = deviceOn;
    }

}
