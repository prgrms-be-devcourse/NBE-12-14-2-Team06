package com.back.nbe12142team06;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Nbe12142Team06Application {

    public static void main(String[] args) {
        SpringApplication.run(Nbe12142Team06Application.class, args);
    }

}
