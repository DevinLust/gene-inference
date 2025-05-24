package com.progressengine.geneinference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class GeneInferenceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		ApplicationContext context = SpringApplication.run(GeneInferenceApplication.class, args);
	}

}
