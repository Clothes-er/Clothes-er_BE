package com.yooyoung.clotheser;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class ClotheserApplication {

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("현재 시각: " + new Date());
    }

    public static void main(String[] args) {
        SpringApplication.run(ClotheserApplication.class, args);
    }

}
