package com.profitchallenge.indicators;

import com.profitchallenge.dto.PriceInfoDto;

import java.util.List;

/**
 * Commodity Channel Index
 */
public class CommodityChannelIndex {

    private double[] cci;

    public void calculate(List<PriceInfoDto> priceInfoDtoList, int range) throws Exception {
        double[] close = priceInfoDtoList.stream()
                .mapToDouble(PriceInfoDto::getTradePrice)
                .toArray();
        double[] high = priceInfoDtoList.stream()
                .mapToDouble(PriceInfoDto::getHighPrice)
                .toArray();
        double[] low = priceInfoDtoList.stream()
                .mapToDouble(PriceInfoDto::getLowPrice)
                .toArray();

        TypicalPrice typicalPrice = new TypicalPrice();
        double[] tp = typicalPrice.calculate(high, low, close).getTypicalPrice();

        SimpleMovingAverage simpleMovingAverage = new SimpleMovingAverage();
        double[] sma = simpleMovingAverage.calculate(tp, range).getSMA();

        this.cci = new double[high.length];

        double[] meanDev = new double[high.length];

        double sum = 0;
        double meanDeviation = 0;

        for (int i = (range - 1); i < close.length; i++) {

            sum = 0;
            meanDeviation = 0;

            for (int j = (i - range + 1); j < (i + 1); j++) {
                sum += Math.abs(tp[j] - sma[i]);
            }

            meanDeviation = sum / range;

            meanDev[i] = meanDeviation;

            if (meanDeviation > 0) {
                this.cci[i] = (tp[i] - sma[i]) / (0.015 * meanDeviation);
            }
            priceInfoDtoList.get(i).setCci(this.cci[i]);
        }

    }

    public double[] getCCI() {
        return this.cci;
    }

}