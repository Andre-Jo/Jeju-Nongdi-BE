package com.jeju_nongdi.jeju_nongdi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling  // AI 팁 스케줄링 활성화
public class JejuNongdiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JejuNongdiApplication.class, args);
	}

}
