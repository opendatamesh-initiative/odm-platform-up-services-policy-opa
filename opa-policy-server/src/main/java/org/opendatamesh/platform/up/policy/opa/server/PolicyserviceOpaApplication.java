package org.opendatamesh.platform.up.policy.opa.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "org.opendatamesh.platform.up.policy")
@EntityScan(basePackages = "org.opendatamesh.platform.up.policy")
@EnableJpaRepositories(basePackages = "org.opendatamesh.platform.up.policy")
public class PolicyserviceOpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolicyserviceOpaApplication.class, args);
	}

}
