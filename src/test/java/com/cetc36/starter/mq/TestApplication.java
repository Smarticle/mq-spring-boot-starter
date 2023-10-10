package com.cetc36.starter.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(TestApplication.class, args);
        Thread.sleep(50000);
    }
}
