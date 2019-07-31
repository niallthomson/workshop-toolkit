package com.devcodes.workshopkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkshopBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkshopBotApplication.class, args);
	}

}
