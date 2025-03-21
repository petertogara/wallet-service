package com.petmuc.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotBlank
    @Schema(description = "Unique ID for the transaction", example = "event-123456")
    private String eventId;

    @NotBlank
    @Schema(description = "Unique player ID", example = "player-001")
    private String playerId;

    @Schema(description = "Name of player", example = "Peter Togara")
    private String name;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Transaction amount", example = "50.00")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Type of transaction: PURCHASE or WIN", example = "PURCHASE")
    private EventType eventType;
}
