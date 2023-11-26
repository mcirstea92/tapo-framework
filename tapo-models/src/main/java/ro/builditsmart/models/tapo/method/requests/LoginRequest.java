package ro.builditsmart.models.tapo.method.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    private String ip;
    private String username;
    private String password;
    private String protocol;

}
