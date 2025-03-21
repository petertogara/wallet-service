package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.exception.InsufficientBalanceException;
import com.petmuc.wallet.model.Wallet;
import com.petmuc.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import({BalanceService.class})
class BalanceServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    void updateBalance_winEvent_shouldUpdateBalance() {

        Wallet wallet = Wallet.builder()
                .playerId("player-001")
                .balance(BigDecimal.valueOf(100))
                .build();

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(50), EventType.WIN
        );

        when(walletRepository.save(any(Wallet.class))).thenReturn(Mono.just(wallet));


        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(balanceService.updateBalance(wallet, request))
                .expectNextMatches(updatedWallet -> updatedWallet.getBalance().equals(BigDecimal.valueOf(150)))
                .verifyComplete();
    }

    @Test
    void updateBalance_purchaseEvent_insufficientBalance_shouldThrowException() {

        Wallet wallet = Wallet.builder()
                .playerId("player-001")
                .balance(BigDecimal.valueOf(50))
                .build();

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(100), EventType.PURCHASE
        );


        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> {
            Mono<?> mono = invocation.getArgument(0);
            return mono.onErrorMap(InsufficientBalanceException.class, ex -> ex);
        });


        StepVerifier.create(balanceService.updateBalance(wallet, request))
                .expectErrorMatches(ex -> ex instanceof InsufficientBalanceException &&
                        ex.getMessage().equals("Insufficient funds. Your balance is 50"))
                .verify();
    }
}