package com.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages={"com.search"})// same as @Configuration @EnableAutoConfiguration @ComponentScan combined
public class FileSearchRestApiApp {

	public static void main(String[] args) {
		SpringApplication.run(FileSearchRestApiApp.class, args);
	}
}
