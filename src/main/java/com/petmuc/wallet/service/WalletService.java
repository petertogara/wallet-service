package com.petmuc.wallet.service;

import com.petmuc.wallet.dto.BalanceResponse;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.dto.TransactionResponse;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<TransactionResponse> processTransaction(TransactionRequest request);

    Mono<BalanceResponse> getBalance(String playerId);

    Mono<TransactionResponse> getTransaction(String eventId);
}
