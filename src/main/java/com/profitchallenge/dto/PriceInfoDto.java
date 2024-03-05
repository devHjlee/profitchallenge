package com.profitchallenge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;


@Getter
@Setter
@ToString
public class PriceInfoDto {

    @Comment("심볼")
    String symbol;

    @Comment("거래 일자")
    String tradeDate;

    @Comment("현재가")
    double tradePrice;

    @Comment("시작가")
    double openingPrice;

    @Comment("고가")
    double highPrice;

    @Comment("저가")
    double lowPrice;

    @Comment("거래량")
    double tradeVolume;

    @Comment("CCI")
    double cci;

    @Comment("볼린저밴드 평균")
    double bbAvg;

    @Comment("볼린저밴드 UP")
    double bbUp;

    @Comment("볼린저밴드 DOWN")
    double bbDown;

    @Comment("RSI")
    double rsi;

    @Comment("MACD")
    double macd;

    @Comment("MACD_EMA_SHORT")
    double macdEmaShort;

    @Comment("MACD_EMA_LONG")
    double macdEmaLong;

    @Comment("MACD_SIGNAL")
    double macdSignal;

    @Comment("MACD_HISTOGRAM")
    double macdSignalHistogram;

    @Comment("ADX")
    double adx;

    @Comment("PARABOLIC_SAR")
    double pSar;

    @Comment("AROON_UP")
    double aroonUp;

    @Comment("AROON_DOWN")
    double aroonDown;

    @Comment("AROON_OSCILLATOR")
    double aroonOscillator;

    @Comment("Stochastic FastK")
    double fastK;

    @Comment("Stochastic FastD")
    double fastD;

    @Comment("Stochastic SlowK")
    double slowK;

    @Comment("Stochastic SlowK")
    double slowD;

    @Comment("단순 MA 10")
    double sma10;

    @Comment("단순 MA 60")
    double sma60;

    @Comment("단순 MA 120")
    double sma120;

    @Comment("EMA")
    double ema10;
}
