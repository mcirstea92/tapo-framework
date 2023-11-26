package ro.builditsmart.connectors.tapo.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RSAKeyPair {

    private String publicKey;

    private String privateKey;

}
