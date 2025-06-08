package com.cognizant.assessment.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class VaultExampleApplication {

	private static Logger LOGGER = LoggerFactory.getLogger(VaultExampleApplication.class);
	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		LOGGER.info("Application starting");
		SpringApplication.run(VaultExampleApplication.class, args);
		LOGGER.info("APPLICATION STARTED");
	}


	/*@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");
			System.out.println("User name : " + environment.getProperty("test.dbuser"));
			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};
	}*/

	/*@Override
	public void run(String... args) throws Exception {
		System.out.println("yyyyyyyyyyyyyyyy");
		for (int i = 0; i < args.length; ++i) {
			System.out.printf("args[{}]: {}", i, args[i]);
		}

	}*/
}
