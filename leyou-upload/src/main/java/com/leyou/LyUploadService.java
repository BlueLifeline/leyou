package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.leyou")
@EnableDiscoveryClient
public class LyUploadService {

    public static void main(String[] args) {
        SpringApplication.run(LyUploadService.class);
    }


}
