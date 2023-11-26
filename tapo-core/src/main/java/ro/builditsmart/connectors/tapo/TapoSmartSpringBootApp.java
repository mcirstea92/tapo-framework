package ro.builditsmart.connectors.tapo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.Charset;

@SpringBootApplication
public class TapoSmartSpringBootApp {

    public static void main(String[] args) {
        SpringApplication.run(TapoSmartSpringBootApp.class);
        System.out.println("Default Charset: " + Charset.defaultCharset());
        System.setProperty("file.encoding", "UTF-8");
        System.out.println("Default Charset (After Change): " + Charset.defaultCharset());
    }

}
