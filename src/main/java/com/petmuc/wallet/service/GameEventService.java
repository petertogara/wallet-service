package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventStatus;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.model.GameEvent;
import com.petmuc.wallet.repository.GameEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameEventService {

    private final GameEventRepository gameEventRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<GameEvent> saveTransaction(TransactionRequest request, BigDecimal newBalance) {
        GameEvent event = GameEvent.builder()
                .eventId(request.getEventId())
                .playerId(request.getPlayerId())
                .eventType(request.getEventType())
                .amount(newBalance)
                .timestamp(Instant.now())
                .status(EventStatus.COMPLETED)
                .build();

        return transactionalOperator.transactional(
                gameEventRepository.save(event)
                        .flatMap(wallet -> gameEventRepository.findByEventId(wallet.getEventId()))
                        .doOnSuccess(wallet -> log.info("Game event created successfully: {}", wallet))
                        .doOnError(ex -> log.error("Game event creation failed with error: {}", ex.getMessage()))
        );
    }

}