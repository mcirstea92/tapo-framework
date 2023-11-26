package ro.builditsmart.rest.tapo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TapoRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TapoRestApplication.class);
    }

}
