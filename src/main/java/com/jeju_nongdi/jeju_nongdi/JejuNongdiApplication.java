package com.jeju_nongdi.jeju_nongdi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JejuNongdiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JejuNongdiApplication.class, args);
	}

}
