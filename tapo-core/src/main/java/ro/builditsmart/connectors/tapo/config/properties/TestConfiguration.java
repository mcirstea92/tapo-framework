package ro.builditsmart.connectors.tapo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "tplink")
@Data
public class TestConfiguration {

    private String email;

    private String password;

    private List<TapoDeviceConfig> devices;

    private CloudConfig cloud;

}
