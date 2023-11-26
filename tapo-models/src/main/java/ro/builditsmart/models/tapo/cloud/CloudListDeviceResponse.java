package ro.builditsmart.models.tapo.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ro.builditsmart.models.tapo.method.responses.TapoResponse;

import java.util.List;

@Data
public class CloudListDeviceResponse extends TapoResponse<CloudListDeviceResponse.CloudListDeviceResult> {

    public static class CloudListDeviceResult {
        @JsonProperty("deviceList")
        private List<TapoDeviceDTO> deviceList;

        public List<TapoDeviceDTO> getDeviceList() {
            return deviceList;
        }

        public void setDeviceList(List<TapoDeviceDTO> deviceList) {
            this.deviceList = deviceList;
        }
    }
}


