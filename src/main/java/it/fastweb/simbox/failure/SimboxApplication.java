package it.fastweb.simbox.failure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
public class SimboxApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SimboxApplication.class, args);
    }
}
