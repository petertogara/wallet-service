package com.petmuc.wallet.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("wallets")
public class Wallet {

    @Id
    private String id;

    private String playerId;

    private String name;

    private BigDecimal balance;
}