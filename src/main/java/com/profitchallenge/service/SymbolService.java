package com.profitchallenge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profitchallenge.bybit.ByBitAPI;
import com.profitchallenge.domain.Symbol;
import com.profitchallenge.dto.SymbolDto;
import com.profitchallenge.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolService {
    private final ByBitAPI byBitAPI;
    private final SymbolRepository symbolRepository;

    @Transactional
    public void saveSymbols() {
        try {
            String response = byBitAPI.getSymbols();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode listNode = rootNode.path("result").path("list");
            List<SymbolDto> symbolDtoList = new ArrayList<>();

            for (JsonNode entry : listNode) {
                if("USDT".equals(entry.get("settleCoin").asText())) {
                    SymbolDto symbolDto = new SymbolDto();
                    symbolDto.setSymbol(entry.get("symbol").asText());
                    symbolDto.setMinOrderQty(entry.path("lotSizeFilter").get("minOrderQty").asDouble());
                    symbolDto.setMinPrice(entry.path("priceFilter").get("minPrice").asDouble());
                    symbolDtoList.add(symbolDto);
                }
            }
            List<Symbol> symbols = symbolRepository.findAll();
            Set<String> keys = symbols.stream().map(Symbol::getSymbol).collect(Collectors.toSet());

            List<SymbolDto> result = symbolDtoList.stream().filter(dto -> !keys.contains(dto.getSymbol())).collect(Collectors.toList());
            for(SymbolDto symbolDto : result) {
                symbolRepository.save(symbolDto.toEntity());
            }

        } catch (IOException e) {
            log.error("saveSymbols Exception : "+e);
            e.printStackTrace();
        }
    }

    public void updateVolume() {

    }
}
