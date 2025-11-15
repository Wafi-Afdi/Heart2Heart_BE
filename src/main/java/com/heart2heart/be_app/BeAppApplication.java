package com.heart2heart.be_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
public class BeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeAppApplication.class, args);
	}
}
