package com.petmuc.wallet.controller;

import com.petmuc.wallet.dto.BalanceResponse;
import com.petmuc.wallet.dto.TransactionRequest;
import com.petmuc.wallet.dto.TransactionResponse;
import com.petmuc.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/transaction")
    public Mono<ResponseEntity<TransactionResponse>> processTransaction(
            @Valid @RequestBody TransactionRequest request) {
        return walletService.processTransaction(request)
                .map(ResponseEntity::ok);
    }


    @GetMapping("/{playerId}/balance")
    public Mono<ResponseEntity<BalanceResponse>> getBalance(@PathVariable String playerId) {
        return walletService.getBalance(playerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @GetMapping("/transactions/{eventId}")
    public Mono<ResponseEntity<TransactionResponse>> getTransaction(@PathVariable String eventId) {
        return walletService.getTransaction(eventId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
