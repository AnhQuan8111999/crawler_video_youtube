package com.ringme.CrawlerData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrawlerDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrawlerDataApplication.class, args);
	}
}
