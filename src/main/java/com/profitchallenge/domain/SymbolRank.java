package com.profitchallenge.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor( access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name="symbol_rank")
public class SymbolRank implements Persistable<RankPK>{
    @EmbeddedId
    private RankPK rankPK;
    private String symbol;
    private double tradeVolume;
    private double rateChange;
    private String status;

    public void updateRaking(String symbol, double tradeVolume, double rateChange, String status) {
        this.symbol = symbol;
        this.tradeVolume = tradeVolume;
        this.rateChange = rateChange;
        this.status = status;
    }

    @Builder
    public SymbolRank(RankPK rankPK, String symbol,double tradeVolume, double rateChange, String status) {
        this.rankPK = rankPK;
        this.symbol = symbol;
        this.tradeVolume = tradeVolume;
        this.rateChange = rateChange;
        this.status = status;
    }

    @Override
    public RankPK getId() {
        return rankPK;
    }

    @Override
    public boolean isNew() {
        return rankPK == null;
    }
}
