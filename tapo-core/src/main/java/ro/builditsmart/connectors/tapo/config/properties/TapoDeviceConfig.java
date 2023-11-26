package ro.builditsmart.connectors.tapo.config.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;

@Data
public class TapoDeviceConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("model")
    private String model;

    @JsonProperty("address")
    private String address;

    /**
     * needs to be a valid enum alias: multi, passthrough, klap
     */
    @JsonProperty("protocol")
    private String protocol;

    public TapoDeviceProtocol getProtocol() {
        return TapoDeviceProtocol.fromAlias(protocol);
    }

}
