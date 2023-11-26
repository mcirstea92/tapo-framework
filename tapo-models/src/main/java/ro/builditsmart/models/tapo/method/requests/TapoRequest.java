package ro.builditsmart.models.tapo.method.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TapoRequest<TParams> {

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private TParams params;

}
