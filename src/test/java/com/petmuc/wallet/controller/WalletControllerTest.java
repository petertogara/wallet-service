package com.petmuc.wallet.controller;

import com.petmuc.wallet.config.NoSecurityConfig;
import com.petmuc.wallet.dto.BalanceResponse;
import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.dto.TransactionResponse;
import com.petmuc.wallet.service.WalletService;
import com.petmuc.wallet.service.impl.WalletServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@WebFluxTest(WalletController.class)
@Import({WalletServiceImpl.class, NoSecurityConfig.class})
class WalletControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WalletService walletService;

    @Test
    void testProcessTransaction_winEvent() {
        TransactionRequest request = new TransactionRequest("tx-001", "player-001", "Faith Mhaka", BigDecimal.valueOf(500), EventType.WIN);

        TransactionResponse response = new TransactionResponse("tx-001", "player-001", BigDecimal.valueOf(500), "SUCCESS");

        when(walletService.processTransaction(any(TransactionRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/wallet/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .value(resp -> assertEquals("SUCCESS", resp.getStatus()));
    }

    @Test
    void testGetBalance_existingPlayer() {
        BalanceResponse response = new BalanceResponse("player-001", BigDecimal.valueOf(500));

        when(walletService.getBalance("player-001")).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/wallet/player-001/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(resp -> assertEquals(BigDecimal.valueOf(500), resp.getBalance()));
    }

    @Test
    void testGetTransaction_found() {
        TransactionResponse response = new TransactionResponse("tx-001", "player-001", BigDecimal.valueOf(500), "SUCCESS");

        when(walletService.getTransaction("tx-001")).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/wallet/transactions/tx-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .value(resp -> assertEquals("tx-001", resp.getEventId()));
    }

    @Test
    void testGetTransaction_notFound() {
        when(walletService.getTransaction("tx-999")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/wallet/transactions/tx-999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
