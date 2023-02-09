package com.example.ProcessLeaveRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = "classpath:BPMN/RequestLeaveProcess.bpmn")
public class ProcessLeaveRequestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessLeaveRequestApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// Do any additional configuration here
		return builder.build();
	}
}
