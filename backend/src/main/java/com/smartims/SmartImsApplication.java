package com.smartims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartImsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartImsApplication.class, args);
	}
}
	