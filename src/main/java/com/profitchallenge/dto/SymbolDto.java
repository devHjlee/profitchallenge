package com.profitchallenge.dto;

import com.profitchallenge.domain.Symbol;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class SymbolDto {

    private String symbol;
    private double minPrice;
    private double minOrderQty;

    @Builder
    public SymbolDto (String symbol, double minPrice, double minOrderQty) {
        this.symbol = symbol;
        this.minPrice = minPrice;
        this.minOrderQty = minOrderQty;
    }

    public Symbol toEntity() {
        return Symbol.builder()
                .symbol(symbol)
                .minPrice(minPrice)
                .minOrderQty(minOrderQty)
                .build();
    }
}
