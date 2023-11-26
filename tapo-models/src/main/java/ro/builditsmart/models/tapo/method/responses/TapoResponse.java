package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TapoResponse<TResult> {

    @JsonProperty("error_code")
    private int errorCode;

    @JsonProperty("result")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TResult result;

}

