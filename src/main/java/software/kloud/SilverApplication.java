package software.kloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("software.kloud")
@EnableJpaRepositories(basePackages = {"software.kloud"})
public class SilverApplication {

    public static void main(String[] args) {
        SpringApplication.run(SilverApplication.class, args);
    }

}
