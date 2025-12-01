package gestion.securelife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching

public class SecureLifeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureLifeApplication.class, args);
    }

}
