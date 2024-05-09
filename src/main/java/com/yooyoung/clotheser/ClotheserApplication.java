package com.yooyoung.clotheser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ClotheserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClotheserApplication.class, args);
    }

}
