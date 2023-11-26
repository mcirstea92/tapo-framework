package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WirelessScanInfoResponse extends TapoResponse<WirelessScanInfoResponse.WirelessScanInfoResult> {

    @Data
    public static class WirelessScanInfoResult {

        @JsonProperty("wep_supported")
        private boolean wepSupported;

        @JsonProperty("ap_list")
        private List<String> apList;

    }
}
