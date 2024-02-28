package com.profitchallenge.indicators;

import com.profitchallenge.dto.PriceInfoDto;

import java.util.List;

public class StochasticsOscilator {
    // 주식 데이터 클래스 정의
    // Fast %K, Fast %D, Slow %K, Slow %D를 계산하는 함수
    // Fast %K = ((현재가 - n기간 중 최저가) / (n기간 중 최고가 - n기간 중 최저가)) * 100
    public double getStochasticFastK(List<PriceInfoDto> data, int currentIndex, int n) {
        double currentClosePrice = data.get(currentIndex).getTradePrice();
        double lowestLow = currentClosePrice;
        double highestHigh = currentClosePrice;

        for (int i = Math.max(0, currentIndex - n + 1); i <= currentIndex; i++) {
            double lowPrice = data.get(i).getLowPrice();
            double highPrice = data.get(i).getHighPrice();
            lowestLow = Math.min(lowestLow, lowPrice);
            highestHigh = Math.max(highestHigh, highPrice);
        }

        return ((currentClosePrice - lowestLow) / (highestHigh - lowestLow)) * 100;
    }

    // Slow %K = Fast %K의 m기간 이동평균(SMA)
    public double getStochasticSlowK(List<PriceInfoDto> data, int currentIndex, int n) {
        double sumFastK = 0;

        for (int i = Math.max(0, currentIndex - n + 1); i <= currentIndex; i++) {
            sumFastK += getStochasticFastK(data, i, 5);
        }

        return sumFastK / Math.min(n, currentIndex + 1);
    }

    // Slow %D = Slow %K의 t기간 이동평균(SMA)
    public double getStochasticSlowD(List<PriceInfoDto> data, int currentIndex, int n) {
        double sumSlowK = 0;

        for (int i = Math.max(0, currentIndex - n + 1); i <= currentIndex; i++) {
            sumSlowK += getStochasticSlowK(data, i, 3);
        }

        return sumSlowK / Math.min(n, currentIndex + 1);
    }

}
