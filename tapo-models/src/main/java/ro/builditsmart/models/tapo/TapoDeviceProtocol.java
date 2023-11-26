package ro.builditsmart.models.tapo;

import java.util.Arrays;

public enum TapoDeviceProtocol {
    Multi(0, "multi"),
    SecurePassThrough(1, "passthrough"),
    Klap(2, "klap");

    int code;

    String alias;

    TapoDeviceProtocol(int val, String alias) {
        this.code = val;
        this.alias = alias;
    }

    public int getCode() {
        return this.code;
    }

    public String getAlias() {
        return this.alias;
    }

    public static TapoDeviceProtocol fromCode(int code) {
        return Arrays.stream(values())
                .filter(tp -> tp.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid code supplied for a TapoDeviceProtocol: " + code));
    }

    public static TapoDeviceProtocol fromAlias(String alias) {
        return Arrays.stream(values())
                .filter(tp -> tp.alias.equalsIgnoreCase(alias))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid alias supplied for a TapoDeviceProtocol: " + alias));
    }

}
