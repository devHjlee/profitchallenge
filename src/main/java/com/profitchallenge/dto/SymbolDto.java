package com.profitchallenge.dto;

import com.profitchallenge.domain.Symbol;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class SymbolDto {

    String symbol;
    double minPrice;
    double minOrderQty;

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
