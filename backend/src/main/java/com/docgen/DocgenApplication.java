package com.docgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Doc4j-style Form Builder platform backend.
 */
@SpringBootApplication
@EnableAsync
public class DocgenApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocgenApplication.class, args);
    }
}
