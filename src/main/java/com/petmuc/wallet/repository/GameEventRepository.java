package com.petmuc.wallet.repository;

import com.petmuc.wallet.model.GameEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface GameEventRepository extends ReactiveCrudRepository<GameEvent, String> {

    @Query("SELECT * FROM game_events WHERE event_id = :eventId LIMIT 1")
    Mono<GameEvent> findByEventId(String eventId);

}