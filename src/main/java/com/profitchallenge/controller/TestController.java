package com.profitchallenge.controller;

import com.profitchallenge.service.PriceInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final PriceInfoService priceInfoService;
    @GetMapping("/test/{symbol}")
    public void test(@PathVariable String symbol) {
        priceInfoService.connectWebSocket(symbol,"1");
    }

    @GetMapping("/test2")
    public List<String> test2() {
        return priceInfoService.getConnectList();
    }

    @GetMapping("/test3/{symbol}")
    public void test3(@PathVariable String symbol) {
        priceInfoService.disconnect(symbol);
    }
}
