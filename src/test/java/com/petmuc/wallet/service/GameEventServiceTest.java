package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.EventStatus;
import com.petmuc.wallet.dto.EventType;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.model.GameEvent;
import com.petmuc.wallet.repository.GameEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameEventServiceTest {

    @Mock
    private GameEventRepository gameEventRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private GameEventService gameEventService;

    @Test
    void saveTransaction_shouldSaveEvent() {

        TransactionRequest request = new TransactionRequest(
                "tx-001", "player-001", "John",
                BigDecimal.valueOf(100), EventType.WIN
        );

        GameEvent event = GameEvent.builder()
                .eventId("tx-001")
                .playerId("player-001")
                .eventType(EventType.WIN)
                .amount(BigDecimal.valueOf(100))
                .timestamp(Instant.now())
                .status(EventStatus.COMPLETED)
                .build();

        when(gameEventRepository.save(any(GameEvent.class))).thenReturn(Mono.just(event));
        when(gameEventRepository.findByEventId("tx-001")).thenReturn(Mono.just(event));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));


        StepVerifier.create(gameEventService.saveTransaction(request, BigDecimal.valueOf(100)))
                .expectNextMatches(savedEvent -> savedEvent.getEventId().equals("tx-001"))
                .verifyComplete();
    }
}
