package ro.builditsmart.models.tapo.method.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import ro.builditsmart.models.tapo.TapoColor;
import ro.builditsmart.models.tapo.TapoSetDeviceState;

import java.util.Objects;

public class TapoSetBulbState extends TapoSetDeviceState {

    @JsonProperty("brightness")
    private Integer brightness;
    @JsonProperty("hue")
    private Integer hue;
    @JsonProperty("saturation")
    private Integer saturation;
    @JsonProperty("color_temp")
    private Integer colorTemperature;

    public TapoSetBulbState(TapoColor color, Boolean deviceOn) {
        setDeviceOn(deviceOn);
        this.hue = color != null ? color.getHue() : null;
        this.saturation = color != null ? color.getSaturation() : null;
        this.colorTemperature = color != null ? color.getColorTemp() : null;
        this.brightness = color != null ? color.getBrightness() : null;
    }

    public TapoSetBulbState(Integer brightness, Boolean deviceOn) {
        this.brightness = brightness;
        setDeviceOn(deviceOn);
    }

    public TapoSetBulbState(Boolean deviceOn) {
        setDeviceOn(deviceOn);
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Integer getHue() {
        return hue;
    }

    public void setHue(Integer hue) {
        this.hue = hue;
    }

    public Integer getSaturation() {
        return saturation;
    }

    public void setSaturation(Integer saturation) {
        this.saturation = saturation;
    }

    public Integer getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Integer colorTemperature) {
        this.colorTemperature = colorTemperature;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TapoSetBulbState that = (TapoSetBulbState) obj;

        if (!Objects.equals(brightness, that.brightness)) return false;
        if (!Objects.equals(hue, that.hue)) return false;
        if (!Objects.equals(saturation, that.saturation)) return false;
        if (!Objects.equals(colorTemperature, that.colorTemperature)) return false;

        return getDeviceOn() != null ? getDeviceOn().equals(that.getDeviceOn()) : that.getDeviceOn() == null;
    }

    @Override
    public int hashCode() {
        int result = brightness != null ? brightness.hashCode() : 0;
        result = 31 * result + (hue != null ? hue.hashCode() : 0);
        result = 31 * result + (saturation != null ? saturation.hashCode() : 0);
        result = 31 * result + (colorTemperature != null ? colorTemperature.hashCode() : 0);
        result = 31 * result + (getDeviceOn() != null ? getDeviceOn().hashCode() : 0);
        return result;
    }
}
