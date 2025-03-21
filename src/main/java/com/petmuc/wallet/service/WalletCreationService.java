package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.exception.PlayerNotFoundException;
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
public class WalletCreationService {

    private final WalletRepository walletRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<Wallet> createNewWalletIfWin(TransactionRequest request) {
        if (EventType.WIN.equals(request.getEventType())) {
            return transactionalOperator.transactional(
                    walletRepository.save(Wallet.builder()
                                    .playerId(request.getPlayerId())
                                    .name(request.getName())
                                    .balance(BigDecimal.ZERO)
                                    .build())
                            .flatMap(wallet -> walletRepository.findByPlayerId(wallet.getPlayerId()))
                            .doOnSuccess(wallet -> log.info("Wallet created successfully: {}", wallet))
                            .doOnError(ex -> log.error("Wallet creation failed with error: {}", ex.getMessage()))
            );
        } else {
            return Mono.error(new PlayerNotFoundException("Player ID not found. You do not have a wallet."));
        }
    }
}