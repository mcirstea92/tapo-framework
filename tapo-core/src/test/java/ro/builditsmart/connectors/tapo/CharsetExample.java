package ro.builditsmart.connectors.tapo;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

public class CharsetExample {

    @Test
    void test() {
        System.out.println("Default Charset: " + Charset.defaultCharset());
        System.out.println("Available charsets: " + Charset.availableCharsets());
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Default Charset (After Change): " + Charset.defaultCharset());
    }

}
