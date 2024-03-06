package com.profitchallenge.dto;

import com.profitchallenge.domain.RankPK;
import com.profitchallenge.domain.SymbolRank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SymbolRankDto {
    private int ranking;
    private String rankDate;
    private String symbol;
    private double tradeVolume;
    private double rateChange;

    @Builder
    public SymbolRankDto (int ranking, String rankDate, String symbol, double tradeVolume, double rateChange) {
        this.ranking = ranking;
        this.rankDate = rankDate;
        this.symbol = symbol;
        this.tradeVolume = tradeVolume;
        this.rateChange = rateChange;
    }

    public SymbolRank toEntity () {
        return SymbolRank.builder()
                .rankPK(new RankPK(ranking,rankDate))
                .symbol(symbol)
                .rateChange(rateChange)
                .tradeVolume(tradeVolume)
                .build();
    }
}
