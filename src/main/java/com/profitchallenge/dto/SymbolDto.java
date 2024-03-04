package com.profitchallenge.dto;

import com.profitchallenge.domain.Symbol;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class SymbolDto {

    String symbol;

    double minOrderQty;

    double minPrice;

    public Symbol toEntity() {
        return Symbol.builder()
                .symbol(symbol)
                .minPrice(minPrice)
                .minOrderQty(minOrderQty)
                .build();
    }
}
