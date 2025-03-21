package com.petmuc.wallet;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestcontainersConfiguration {

	private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("test_wallet_db")
			.withUsername("test_user")
			.withPassword("test_pass");

	static {
		POSTGRES_CONTAINER.start();
		System.setProperty("spring.r2dbc.url", "r2dbc:postgresql://" + POSTGRES_CONTAINER.getHost() + ":" + POSTGRES_CONTAINER.getFirstMappedPort() + "/test_wallet_db");
		System.setProperty("spring.r2dbc.username", "test_user");
		System.setProperty("spring.r2dbc.password", "test_pass");
	}
}