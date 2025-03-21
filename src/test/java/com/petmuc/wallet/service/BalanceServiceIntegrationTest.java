package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.model.Wallet;
import com.petmuc.wallet.repository.WalletRepository;
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
class BalanceServiceIntegrationTest {

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
    private BalanceService balanceService;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void updateBalance_winEvent_shouldUpdateBalance() {

        Wallet wallet = Wallet.builder()
                .playerId("player-001")
                .balance(BigDecimal.valueOf(100))
                .build();

        walletRepository.save(wallet).block();

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(50), EventType.WIN
        );

        StepVerifier.create(balanceService.updateBalance(wallet, request))
                .expectNextMatches(updatedWallet -> updatedWallet.getBalance().equals(BigDecimal.valueOf(150)))
                .verifyComplete();
    }
}