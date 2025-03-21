package com.petmuc.wallet.model;

import com.petmuc.wallet.dto.EventStatus;
import com.petmuc.wallet.dto.EventType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("game_events")
public class GameEvent {

    @Id
    private String id;

    private String eventId;

    private String playerId;

    private EventType eventType;

    private BigDecimal amount;

    private Instant timestamp;

    private EventStatus status;
}