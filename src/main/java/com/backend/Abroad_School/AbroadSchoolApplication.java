package com.backend.Abroad_School;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AbroadSchoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbroadSchoolApplication.class, args);
	}

}
