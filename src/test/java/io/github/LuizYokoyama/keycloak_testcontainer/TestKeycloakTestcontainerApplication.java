package io.github.LuizYokoyama.keycloak_testcontainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestKeycloakTestcontainerApplication {

	public static void main(String[] args) {
		SpringApplication.from(KeycloakTestcontainerApplication::main).with(TestKeycloakTestcontainerApplication.class).run(args);
	}

}
