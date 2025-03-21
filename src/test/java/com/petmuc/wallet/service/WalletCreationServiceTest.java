package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.exception.PlayerNotFoundException;
import com.petmuc.wallet.model.Wallet;
import com.petmuc.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletCreationServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private WalletCreationService walletCreationService;

    @Test
    void createNewWalletIfWin_winEvent_shouldCreateWallet() {

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(100), EventType.WIN
        );

        Wallet wallet = Wallet.builder()
                .playerId("player-001")
                .name("John")
                .balance(BigDecimal.ZERO)
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(Mono.just(wallet));
        when(walletRepository.findByPlayerId("player-001")).thenReturn(Mono.just(wallet));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));


        StepVerifier.create(walletCreationService.createNewWalletIfWin(request))
                .expectNextMatches(savedWallet -> savedWallet.getPlayerId().equals("player-001"))
                .verifyComplete();
    }

    @Test
    void createNewWalletIfWin_purchaseEvent_shouldThrowException() {

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(100), EventType.PURCHASE
        );


        StepVerifier.create(walletCreationService.createNewWalletIfWin(request))
                .expectErrorMatches(ex -> ex instanceof PlayerNotFoundException &&
                        ex.getMessage().equals("Player ID not found. You do not have a wallet."))
                .verify();
    }
}