package com.study.reactiveprogramming.sample.third;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class _08_02_RemoteApplication {

    @RestController
    public static class RemoteService {
        @GetMapping("/remote")
        public String remote(String req) throws InterruptedException {
            Thread.sleep(1000);
            return "remote " + req;
        }
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        System.setProperty("server.tomcat.threads.max", "1000");

        SpringApplication.run(_08_02_RemoteApplication.class, args);
    }

}
