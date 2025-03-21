package com.petmuc.wallet.service.impl;

import com.petmuc.wallet.dto.BalanceResponse;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.dto.TransactionResponse;
import com.petmuc.wallet.exception.InsufficientBalanceException;
import com.petmuc.wallet.exception.PlayerNotFoundException;
import com.petmuc.wallet.exception.TransactionNotFoundException;
import com.petmuc.wallet.model.GameEvent;
import com.petmuc.wallet.repository.GameEventRepository;
import com.petmuc.wallet.repository.WalletRepository;
import com.petmuc.wallet.service.BalanceService;
import com.petmuc.wallet.service.GameEventService;
import com.petmuc.wallet.service.WalletCreationService;
import com.petmuc.wallet.service.WalletService;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final GameEventRepository gameEventRepository;
    private final TransactionalOperator transactionalOperator;
    private final BalanceService balanceService;
    private final WalletCreationService walletCreationService;
    private final GameEventService gameEventService;


    @Override
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackTransaction")
    public Mono<TransactionResponse> processTransaction(TransactionRequest request) {
        log.info("Processing transaction: {}", request);

        return gameEventRepository.findByEventId(request.getEventId())
                .flatMap(existingEvent -> {
                    log.info("Transaction already processed: EventID={}", request.getEventId());
                    return buildTransactionResponse(existingEvent, "SUCCESS");
                })
                .switchIfEmpty(processNewTransaction(request))
                .transformDeferred(RetryOperator.of(getRetryConfig()));
    }

    private Mono<TransactionResponse> processNewTransaction(TransactionRequest request) {
        log.info("Received new transaction request: {}", request);

        return transactionalOperator.transactional(
                walletRepository.findByPlayerId(request.getPlayerId())
                        .switchIfEmpty(walletCreationService.createNewWalletIfWin(request))
                        .flatMap(wallet -> balanceService.updateBalance(wallet, request))
                        .flatMap(updatedWallet -> gameEventService.saveTransaction(request, updatedWallet.getBalance()))
                        .flatMap(event -> buildTransactionResponse(event, "SUCCESS"))
        );
    }
    private Mono<TransactionResponse> fallbackTransaction(TransactionRequest request, Throwable ex) {
        log.error("Transaction failed after retries. Circuit breaker activated. EventID={}, Reason={}", request.getEventId(), ex.getMessage());
        return Mono.just(new TransactionResponse(request.getEventId(), request.getPlayerId(), BigDecimal.ZERO, "FAILED - Service unavailable"));
    }

    private Retry getRetryConfig() {
        return Retry.of("walletServiceRetry",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(2000))
                        .retryExceptions(IOException.class, SQLException.class)
                        .ignoreExceptions(PlayerNotFoundException.class, InsufficientBalanceException.class)
                        .build()
        );
    }

    @Override
    public Mono<BalanceResponse> getBalance(String playerId) {
        return walletRepository.findByPlayerId(playerId)
                .map(wallet -> new BalanceResponse(wallet.getPlayerId(), wallet.getBalance()))
                .switchIfEmpty(Mono.just(new BalanceResponse(playerId, BigDecimal.ZERO)));
    }

    @Override
    public Mono<TransactionResponse> getTransaction(String eventId) {
        return gameEventRepository.findByEventId(eventId)
                .flatMap(event -> buildTransactionResponse(event, "SUCCESS"))
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction not found")));
    }

    private Mono<TransactionResponse> buildTransactionResponse(GameEvent event, String status) {
        return Mono.just(new TransactionResponse(
                event.getEventId(),
                event.getPlayerId(),
                event.getAmount(),
                status
        ));
    }
}