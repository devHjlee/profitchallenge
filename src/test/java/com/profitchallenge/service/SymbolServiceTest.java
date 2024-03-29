package com.profitchallenge.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SymbolServiceTest {

    @Autowired
    private SymbolService symbolService;

    @Test
    void saveSymbols() {
        symbolService.saveSymbols();
    }
    @Test
    void saveSymbolsRank() { symbolService.saveSymbolRank(); }
}