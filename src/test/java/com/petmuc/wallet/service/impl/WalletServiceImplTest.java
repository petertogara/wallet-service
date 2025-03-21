package com.petmuc.wallet.service.impl;

import com.petmuc.wallet.dto.EventStatus;
import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.exception.TransactionNotFoundException;
import com.petmuc.wallet.model.GameEvent;
import com.petmuc.wallet.model.Wallet;
import com.petmuc.wallet.repository.GameEventRepository;
import com.petmuc.wallet.repository.WalletRepository;
import com.petmuc.wallet.service.BalanceService;
import com.petmuc.wallet.service.GameEventService;
import com.petmuc.wallet.service.WalletCreationService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private GameEventRepository gameEventRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private BalanceService balanceService;

    @Mock
    private WalletCreationService walletCreationService;

    @Mock
    private GameEventService gameEventService;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void processTransaction_transactionAlreadyExists_shouldReturnSuccessResponse() {

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(100), EventType.WIN
        );

        GameEvent existingEvent = GameEvent.builder()
                .eventId("tx-001")
                .playerId("player-001")
                .amount(BigDecimal.valueOf(100))
                .eventType(EventType.WIN)
                .status(EventStatus.COMPLETED)
                .build();

        when(gameEventRepository.findByEventId("tx-001"))
                .thenReturn(Mono.just(existingEvent));


        when(walletRepository.findByPlayerId("player-001")).thenReturn(Mono.empty());

        Wallet newWallet = Wallet.builder()
                .playerId("player-001")
                .name("John")
                .balance(BigDecimal.ZERO)
                .build();
        when(walletCreationService.createNewWalletIfWin(request))
                .thenReturn(Mono.just(newWallet));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        StepVerifier.create(walletService.processTransaction(request))
                .expectNextMatches(resp ->
                        "tx-001".equals(resp.getEventId()) &&
                                "player-001".equals(resp.getPlayerId()) &&
                                "SUCCESS".equals(resp.getStatus()))
                .verifyComplete();

        verify(gameEventRepository).findByEventId("tx-001");
        verify(walletRepository).findByPlayerId("player-001");
        verify(walletCreationService).createNewWalletIfWin(request);
        verifyNoMoreInteractions(walletRepository, balanceService, walletCreationService, gameEventService);
    }
    @Test
    void processTransaction_newWinTransaction_shouldCreateWalletAndSaveTransaction() {
        TransactionRequest request = new TransactionRequest("tx-999", "player-999", "Maria", BigDecimal.valueOf(250), EventType.WIN);

        Wallet newWallet = Wallet.builder()
                .playerId("player-999")
                .name("Maria")
                .balance(BigDecimal.ZERO)
                .build();

        Wallet updatedWallet = Wallet.builder()
                .playerId("player-999")
                .name("Maria")
                .balance(BigDecimal.valueOf(250))
                .build();

        GameEvent gameEvent = GameEvent.builder()
                .eventId("tx-999")
                .playerId("player-999")
                .eventType(EventType.WIN)
                .amount(BigDecimal.valueOf(250))
                .status(EventStatus.COMPLETED)
                .build();

        when(gameEventRepository.findByEventId("tx-999")).thenReturn(Mono.empty());
        when(walletRepository.findByPlayerId("player-999")).thenReturn(Mono.empty());
        when(walletCreationService.createNewWalletIfWin(request)).thenReturn(Mono.just(newWallet));
        when(balanceService.updateBalance(newWallet, request)).thenReturn(Mono.just(updatedWallet));
        when(gameEventService.saveTransaction(request, updatedWallet.getBalance())).thenReturn(Mono.just(gameEvent));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(walletService.processTransaction(request))
                .expectNextMatches(resp ->
                        resp.getEventId().equals("tx-999") &&
                                resp.getPlayerId().equals("player-999") &&
                                resp.getStatus().equals("SUCCESS"))
                .verifyComplete();
    }

    @Test
    void getBalance_existingPlayer_shouldReturnBalance() {
        Wallet wallet = Wallet.builder()
                .playerId("player-100")
                .balance(BigDecimal.valueOf(999))
                .build();

        when(walletRepository.findByPlayerId("player-100")).thenReturn(Mono.just(wallet));

        StepVerifier.create(walletService.getBalance("player-100"))
                .expectNextMatches(resp ->
                        resp.getPlayerId().equals("player-100") &&
                                resp.getBalance().compareTo(BigDecimal.valueOf(999)) == 0)
                .verifyComplete();
    }

    @Test
    void getTransaction_transactionExists_shouldReturnResponse() {
        GameEvent event = GameEvent.builder()
                .eventId("tx-222")
                .playerId("player-222")
                .amount(BigDecimal.valueOf(222))
                .eventType(EventType.WIN)
                .status(EventStatus.COMPLETED)
                .build();

        when(gameEventRepository.findByEventId("tx-222")).thenReturn(Mono.just(event));

        StepVerifier.create(walletService.getTransaction("tx-222"))
                .expectNextMatches(resp ->
                        resp.getEventId().equals("tx-222") &&
                                resp.getPlayerId().equals("player-222") &&
                                resp.getStatus().equals("SUCCESS"))
                .verifyComplete();
    }

    @Test
    void getTransaction_notFound_shouldReturnError() {
        when(gameEventRepository.findByEventId("tx-missing")).thenReturn(Mono.empty());

        StepVerifier.create(walletService.getTransaction("tx-missing"))
                .expectError(TransactionNotFoundException.class)
                .verify();
    }
}

