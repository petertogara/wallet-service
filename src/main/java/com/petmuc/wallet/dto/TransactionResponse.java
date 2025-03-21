package com.petmuc.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    @Schema(description = "Transaction event ID", example = "event-123456")
    private String eventId;

    @Schema(description = "Player ID", example = "player-001")
    private String playerId;

    @Schema(description = "Updated player balance after transaction", example = "450.00")
    private BigDecimal newBalance;

    @Schema(description = "Transaction status: SUCCESS or FAILED", example = "SUCCESS")
    private String status;
}
