package com.petmuc.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    @Schema(description = "Unique identifier of the player", example = "player-001")
    private String playerId;

    @Schema(description = "Current available balance of the player", example = "450.00")
    private BigDecimal balance;
}
