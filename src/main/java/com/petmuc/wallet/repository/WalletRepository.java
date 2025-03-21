package com.petmuc.wallet.repository;

import com.petmuc.wallet.model.Wallet;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WalletRepository extends ReactiveCrudRepository<Wallet, String> {

    @Query("SELECT * FROM wallets WHERE player_id = :playerId LIMIT 1")
    Mono<Wallet> findByPlayerId(String playerId);

}
