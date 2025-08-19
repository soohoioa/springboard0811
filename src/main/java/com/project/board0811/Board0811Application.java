package com.project.board0811;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Board0811Application {

    public static void main(String[] args) {
        SpringApplication.run(Board0811Application.class, args);
    }

}
