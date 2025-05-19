package com.progressengine.geneinference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class GeneInferenceApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(GeneInferenceApplication.class, args);
	}

}
