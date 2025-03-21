package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.exception.InsufficientBalanceException;
import com.petmuc.wallet.model.Wallet;
import com.petmuc.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {
    private final WalletRepository walletRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<Wallet> updateBalance(Wallet wallet, TransactionRequest request) {
        log.info("Updating balance: PlayerID={}, Amount={}, Type={}",
                wallet.getPlayerId(), request.getAmount(), request.getEventType());

        return transactionalOperator.transactional(Mono.defer(() -> {
            if (EventType.PURCHASE.equals(request.getEventType())) {
                return handlePurchase(wallet, request);
            } else if (EventType.WIN.equals(request.getEventType())) {
                return handleWin(wallet, request);
            } else {
                log.warn("Invalid event type received: EventID={}, PlayerID={}, Type={}",
                        request.getEventId(), request.getPlayerId(), request.getEventType());
                return Mono.error(new IllegalArgumentException("Invalid event type: " + request.getEventType()));
            }
        }));
    }

    private Mono<Wallet> handlePurchase(Wallet wallet, TransactionRequest request) {
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient funds: PlayerID={}, Balance={}, Requested={}",
                    wallet.getPlayerId(), wallet.getBalance(), request.getAmount());
            return Mono.error(new InsufficientBalanceException(
                    "Insufficient funds. Your balance is " + wallet.getBalance()));
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        wallet.setBalance(newBalance);

        log.info("Purchase approved: PlayerID={}, NewBalance={}, Deducted={}",
                wallet.getPlayerId(), newBalance, request.getAmount());

        return transactionalOperator.transactional(
                walletRepository.save(wallet)
                        .doOnSuccess(savedWallet -> log.info("Purchase saved successfully: {}", savedWallet))
                        .doOnError(ex -> log.error("Purchase failed with error: {}", ex.getMessage()))
        );
    }

    private Mono<Wallet> handleWin(Wallet wallet, TransactionRequest request) {
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);

        log.info("Win to be credited : PlayerID={}, NewBalance={}, Credited={}",
                wallet.getPlayerId(), newBalance, request.getAmount());

        return transactionalOperator.transactional(
                walletRepository.save(wallet)
                        .doOnSuccess(savedWallet -> log.info("Win credited to wallet successfully: {}", savedWallet))
                        .doOnError(ex -> log.error("Win update failed: {}", ex.getMessage()))
        );
    }
}
