package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@Testcontainers
@SpringBootTest
class GameEventServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/test_db");
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private GameEventService gameEventService;

    @Test
    void saveTransaction_shouldSaveEvent() {

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(100), EventType.WIN
        );

        StepVerifier.create(gameEventService.saveTransaction(request, BigDecimal.valueOf(100)))
                .expectNextMatches(event -> event.getEventId().equals("tx-001"))
                .verifyComplete();
    }
}