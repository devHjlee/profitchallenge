package com.profitchallenge.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PriceInfoServiceTest {
    @Autowired
    private PriceInfoService priceInfoService;
    @Test
    void tradeConnect() throws Exception {
        priceInfoService.setNvWebSocket("BTCUSDT");
    }
}