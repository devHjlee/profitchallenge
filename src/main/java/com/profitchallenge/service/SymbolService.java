package com.profitchallenge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profitchallenge.bybit.ByBitAPI;
import com.profitchallenge.domain.Symbol;
import com.profitchallenge.dto.PriceInfoDto;
import com.profitchallenge.dto.SymbolDto;
import com.profitchallenge.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolService {
    private final ByBitAPI byBitAPI;
    private final SymbolRepository symbolRepository;

    @Transactional
    public void saveSymbols() {
        try {
            //저장된 전체 심볼 조회
            List<Symbol> originSymbols = symbolRepository.findAll();
            //바이비트 전체 심볼 조회
            String response = byBitAPI.getSymbols();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode listNode = rootNode.path("result").path("list");
            List<SymbolDto> apiSymbols = new ArrayList<>();

            for (JsonNode entry : listNode) {
                if ("USDT".equals(entry.get("settleCoin").asText())) {
                    apiSymbols.add(SymbolDto.builder()
                            .symbol(entry.get("symbol").asText())
                            .minPrice(entry.path("priceFilter").get("minPrice").asDouble())
                            .minOrderQty(entry.path("lotSizeFilter").get("minOrderQty").asDouble())
                            .build()
                    );
                }
            }

            //새로운 심볼 저장
            apiSymbols.stream()
                    .filter(api -> originSymbols.stream()
                            .noneMatch(entity -> entity.getSymbol().equals(api.getSymbol())))
                    .forEach(api->{
                            symbolRepository.save(api.toEntity());
                        }
                     );

            //심볼 정보중 금액,수량 변경에 대한 정보 업데이트
            apiSymbols.stream()
                    .filter(api -> originSymbols.stream()
                            .anyMatch(entity -> entity.getSymbol().equals(api.getSymbol()) && (entity.getMinPrice() != api.getMinPrice() || entity.getMinOrderQty() != api.getMinOrderQty()) )
                    )
                    .forEach(api->{
                            for (Symbol symbol : originSymbols) {
                                if (symbol.getSymbol().equals(api.getSymbol())) {
                                    symbol.updateSymbolInfo(api.getMinPrice(), api.getMinOrderQty());
                                }
                            }
                        }
                    );

        } catch (IOException e) {
            log.error("saveSymbols Exception : "+e);
        }
    }

    @Transactional
    public void saveSymbolsRank() {

        long startTime = System.currentTimeMillis();
        List<Symbol> symbols = symbolRepository.findAll();
        for (Symbol symbol: symbols) {
            try {

                String response = byBitAPI.getCandle(symbol.getSymbol(),"D","1");
                List<PriceInfoDto> priceInfoList = new ArrayList<>();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode listNode = rootNode.path("result").path("list");
                JsonNode symbolNode = rootNode.path("result").path("symbol");

                for (JsonNode entry : listNode) {
                    PriceInfoDto priceInfo = PriceInfoDto.builder()
                            .symbol(symbolNode.asText())
                            .tradeDate(convertDate(entry.get(0).asText()))
                            .openingPrice(Double.parseDouble(entry.get(1).asText()))
                            .highPrice(Double.parseDouble(entry.get(2).asText()))
                            .lowPrice(Double.parseDouble(entry.get(3).asText()))
                            .tradePrice(Double.parseDouble(entry.get(4).asText()))
                            .tradeVolume(Double.parseDouble(entry.get(5).asText()))
                            .build();

                    priceInfoList.add(priceInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 프로그램 종료 시간 기록
        long endTime = System.currentTimeMillis();                // 실행 시간 계산
        long executionTime = endTime - startTime;                // 실행 시간 출력
        System.out.println(" 프로그램 실행 시간: " + executionTime + " 밀리초");

    }

    //타임스탬프 변환
    private String convertDate(String timeStamp) {
        long timestamp = Long.parseLong(timeStamp);

        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return localDateTime.format(formatter);
    }
}
