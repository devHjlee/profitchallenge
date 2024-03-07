package com.profitchallenge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profitchallenge.bybit.ByBitAPI;
import com.profitchallenge.domain.Symbol;
import com.profitchallenge.domain.SymbolRank;
import com.profitchallenge.dto.SymbolDto;
import com.profitchallenge.dto.SymbolRankDto;
import com.profitchallenge.repository.SymbolRankRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolService {
    private final ByBitAPI byBitAPI;
    private final SymbolRepository symbolRepository;
    private final SymbolRankRepository symbolRankRepository;

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


            apiSymbols.forEach(api -> {
                //새로운 심볼 저장
                if (originSymbols.stream().noneMatch(entity -> entity.getSymbol().equals(api.getSymbol()))) {
                    symbolRepository.save(api.toEntity());
                } else {
                    // 심볼 정보가 변경된 경우 업데이트
                    originSymbols.stream()
                            .filter(symbol -> symbol.getSymbol().equals(api.getSymbol()))
                            .filter(symbol -> symbol.getMinPrice() != api.getMinPrice() || symbol.getMinOrderQty() != api.getMinOrderQty())
                            .forEach(symbol -> symbol.updateSymbolInfo(api.getMinPrice(), api.getMinOrderQty()));
                }
            });

        } catch (IOException e) {
            log.error("saveSymbols Exception : "+e);
        }
    }

    @Transactional
    public void saveSymbolRank() {
        List<SymbolRankDto> symbolRankDtoList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        List<Symbol> symbols = symbolRepository.findAll();
        List<SymbolRank> symbolRanks = symbolRankRepository.findByRankPKRankDate("2");

        for (Symbol symbol: symbols) {
            try {

                String response = byBitAPI.getCandle(symbol.getSymbol(),"D","1");
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode listNode = rootNode.path("result").path("list");
                JsonNode symbolNode = rootNode.path("result").path("symbol");

                for (JsonNode entry : listNode) {
                    double tradeVolume = Double.parseDouble(entry.get(1).asText()) * Double.parseDouble(entry.get(5).asText());
                    double rateChange = ((Double.parseDouble(entry.get(4).asText()) - Double.parseDouble(entry.get(1).asText()))/Double.parseDouble(entry.get(1).asText()))*100;
                    SymbolRankDto symbolRankDto = SymbolRankDto.builder()
                            .ranking(0)
                            .rankDate(convertDate(entry.get(0).asText()))
                            .symbol(symbolNode.asText())
                            .tradeVolume(tradeVolume)
                            .rateChange(rateChange)
                            .build();
                    symbolRankDtoList.add(symbolRankDto);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Bybit에서 받아온 일별 거래대금(시작가*거래량) 기준으로 정렬
        symbolRankDtoList.sort(Comparator.comparingDouble(SymbolRankDto::getTradeVolume).reversed());
        //랭크설정
        IntStream.range(0,symbolRankDtoList.size()).forEach(i -> symbolRankDtoList.get(i).setRanking(i+1));
        //1~10위까지만 남기고 삭제
        symbolRankDtoList.subList(10,symbolRankDtoList.size()).clear();

        if (symbolRanks.isEmpty()) {
            for (SymbolRankDto symbolRankDto : symbolRankDtoList) {
                symbolRankRepository.save(symbolRankDto.toEntity());
            }
        } else {

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
