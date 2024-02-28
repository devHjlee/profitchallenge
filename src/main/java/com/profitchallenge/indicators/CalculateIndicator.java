package com.profitchallenge.indicators;

import com.profitchallenge.dto.PriceInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class CalculateIndicator {
    public void calculate(List<PriceInfoDto> priceInfoDtoList) {
        try {
            priceInfoDtoList.sort(Comparator.comparing(PriceInfoDto::getTradeDate));
            getMACD(priceInfoDtoList);
            getRSI(priceInfoDtoList);
            getCCI(priceInfoDtoList);
            getBollingerBand(priceInfoDtoList);
            //getADX(priceInfoDtoList);
            //getPSar(priceInfoDtoList);
            //getAroon(priceInfoDtoList);
            getStochastics(priceInfoDtoList);
            getSMA(priceInfoDtoList);
            getEma(priceInfoDtoList);
            priceInfoDtoList.sort(Comparator.comparing(PriceInfoDto::getTradeDate).reversed());
        }catch (Exception e) {
            throw new RuntimeException("calculate :"+ e.getMessage());
        }
    }


    /**
     * CommodityChannelIndex 계산
     * @param priceInfoDtoList
     */
    private void getCCI(List<PriceInfoDto> priceInfoDtoList) {
        CommodityChannelIndex cci = new CommodityChannelIndex();
        try {
            cci.calculate(priceInfoDtoList,20);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 볼린저밴드 계산
     * @param priceInfoDtoList
     */
    private void getBollingerBand(List<PriceInfoDto> priceInfoDtoList) {
        BollingerBand bollingerBand= new BollingerBand();
        bollingerBand.calculate(priceInfoDtoList,20,2);

    }
    /**
     * RSI 계산
     * @param priceInfoDtoList
     * @return double
     */
    private void getRSI(List<PriceInfoDto> priceInfoDtoList){
        RelativeStrengthIndex relativeStrengthIndex = new RelativeStrengthIndex();

        try {
            relativeStrengthIndex.calculate(priceInfoDtoList,14);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * MACD 계산
     * @param priceInfoDtoList
     */
    private void getMACD(List<PriceInfoDto> priceInfoDtoList) {
        MovingAverageConvergenceDivergence macd = new MovingAverageConvergenceDivergence();
        try {
            macd.calculate(priceInfoDtoList,12,26,9);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * ADX 계산
     * @param priceInfoDtoList
     */
    private void getADX(List<PriceInfoDto> priceInfoDtoList) {
        AverageDirectionalIndex adx = new AverageDirectionalIndex();
        try {
            adx.calculate(priceInfoDtoList,14);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * ParabolicSar 계산
     * @param priceInfoDtoList
     */
    private void getPSar(List<PriceInfoDto> priceInfoDtoList) {
        ParabolicSar pSar = new ParabolicSar();
        try {
            pSar.calculate(priceInfoDtoList);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Aroon 계산
     * @param priceInfoDtoList
     */
    private void getAroon(List<PriceInfoDto> priceInfoDtoList) {
        Aroon aroon = new Aroon();
        try {
            aroon.calculateAroonOscillator(priceInfoDtoList,14);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Stochastic 계산
     * @param priceInfoDtoList
     */
    private void getStochastics(List<PriceInfoDto> priceInfoDtoList) {
        StochasticsOscilator stochasticsOscilator = new StochasticsOscilator();
        int n = 5; // Fast %K를 계산하는 데 사용되는 기간
        int m = 3; // Slow %K를 계산하는 데 사용되는 기간
        int t = 3; // Slow %D를 계산하는 데 사용되는 기간
        for (int i = 0; i < priceInfoDtoList.size(); i++) {
            double fastK = (Double.isNaN(stochasticsOscilator.getStochasticFastK(priceInfoDtoList, i, n))) ? 0.0 : stochasticsOscilator.getStochasticFastK(priceInfoDtoList, i, n);
            double fastD = (Double.isNaN(stochasticsOscilator.getStochasticSlowK(priceInfoDtoList, i, m))) ? 0.0 : stochasticsOscilator.getStochasticSlowK(priceInfoDtoList, i, m);
            double slowK = (Double.isNaN(stochasticsOscilator.getStochasticSlowK(priceInfoDtoList, i, m))) ? 0.0 : stochasticsOscilator.getStochasticSlowK(priceInfoDtoList, i, m);
            double slowD = (Double.isNaN(stochasticsOscilator.getStochasticSlowD(priceInfoDtoList, i, t))) ? 0.0 : stochasticsOscilator.getStochasticSlowD(priceInfoDtoList, i, t);
            priceInfoDtoList.get(i).setFastK(fastK);
            priceInfoDtoList.get(i).setFastD(fastD);
            priceInfoDtoList.get(i).setSlowK(slowK);
            priceInfoDtoList.get(i).setSlowD(slowD);
        }
    }

    private void getSMA(List<PriceInfoDto> priceInfoDtoList) {
        double[] prices = priceInfoDtoList.stream()
                .mapToDouble(PriceInfoDto::getTradePrice)
                .toArray();
        SimpleMovingAverage simpleMovingAverage = new SimpleMovingAverage();
        double[] sma10 = new double[priceInfoDtoList.size()];
        double[] sma60 = new double[priceInfoDtoList.size()];
        double[] sma120 = new double[priceInfoDtoList.size()];
        try {
            if(prices.length > 10) sma10 = simpleMovingAverage.calculate(prices,10).getSMA();
            if(prices.length > 60) sma60 = simpleMovingAverage.calculate(prices,60).getSMA();
            if(prices.length > 120) sma120 = simpleMovingAverage.calculate(prices,120).getSMA();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < priceInfoDtoList.size(); i++) {
            PriceInfoDto priceInfoDto = priceInfoDtoList.get(i);
            priceInfoDto.setSma10(sma10[i]);
            priceInfoDto.setSma60(sma60[i]);
            priceInfoDto.setSma120(sma120[i]);
        }

    }

    public void getEma(List<PriceInfoDto> priceInfoDtoList) {
        double[] prices = priceInfoDtoList.stream()
                .mapToDouble(PriceInfoDto::getTradePrice)
                .toArray();
        ExponentialMovingAverage exponentialMovingAverage = new ExponentialMovingAverage();
        double[] ema10 = new double[priceInfoDtoList.size()];
        try {
            ema10 = exponentialMovingAverage.calculate(prices,10).getEMA();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < priceInfoDtoList.size(); i++) {
            PriceInfoDto priceInfoDto = priceInfoDtoList.get(i);
            priceInfoDto.setEma10(ema10[i]);
        }
    }
}
