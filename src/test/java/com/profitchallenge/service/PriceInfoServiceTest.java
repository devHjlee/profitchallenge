package com.profitchallenge.service;

import com.profitchallenge.bybit.ByBitAPI;
import com.profitchallenge.dto.PriceInfoDto;
import com.profitchallenge.indicators.CalculateIndicator;
import com.profitchallenge.service.PriceInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class PriceInfoServiceTest {

    @Autowired
    private PriceInfoService priceInfoService;
    @Test
    void saveCandle() {
        priceInfoService.saveCandleInfo("BIGTIMEUSDT","1");
    }
}
