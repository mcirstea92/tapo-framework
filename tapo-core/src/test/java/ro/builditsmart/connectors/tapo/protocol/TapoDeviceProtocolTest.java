package ro.builditsmart.connectors.tapo.protocol;

import org.junit.jupiter.api.Test;
import ro.builditsmart.models.tapo.TapoDeviceProtocol;

import static org.junit.jupiter.api.Assertions.*;

class TapoDeviceProtocolTest {

    @Test
    void getCodeAndAlias() {
        TapoDeviceProtocol protocol = TapoDeviceProtocol.SecurePassThrough;
        assertEquals(1, protocol.getCode(), "The code for SecurePassThrough should be 1");
        assertEquals("passthrough", protocol.getAlias(), "The alias for SecurePassThrough should be 'passthrough'");
    }

    @Test
    void fromCodeAndAlias() {
        TapoDeviceProtocol protocol = TapoDeviceProtocol.fromCode(0);
        assertEquals("multi", protocol.getAlias(), "The alias for the protocol from code 0 should be 'multi'");
        protocol = TapoDeviceProtocol.fromAlias("klap");
        assertEquals(2, protocol.getCode(), "The code for the protocol from alias 'klap' should be 2");

        IllegalArgumentException invalidCodeException = assertThrows(IllegalArgumentException.class,
                () -> TapoDeviceProtocol.fromCode(45));
        assertEquals("Invalid code supplied for a TapoDeviceProtocol: 45", invalidCodeException.getMessage(), "Message of the exception should match");
        IllegalArgumentException invalidAliasException = assertThrows(IllegalArgumentException.class,
                () -> TapoDeviceProtocol.fromAlias("invalid-alias"));
        assertEquals("Invalid alias supplied for a TapoDeviceProtocol: invalid-alias", invalidAliasException.getMessage(), "Message of the exception should match");
    }

}