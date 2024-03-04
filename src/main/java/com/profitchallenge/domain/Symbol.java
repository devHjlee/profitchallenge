package com.profitchallenge.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.LocalDateTime;

@NoArgsConstructor( access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name="symbol")
public class Symbol {
    @Id
    private String symbol;

    private double minPrice;

    private double minOrderQty;

    @CreatedDate
    private LocalDateTime createDate;


    @Builder
    public Symbol(String symbol, double minPrice, double minOrderQty) {
        this.symbol = symbol;
        this.minPrice = minPrice;
        this.minOrderQty = minOrderQty;
    }

    public void updateSymbolInfo(double minPrice, double minOrderQty) {
        this.minPrice = minPrice;
        this.minOrderQty  = minOrderQty;
    }
}
