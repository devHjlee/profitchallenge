package com.profitchallenge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.*;
import com.profitchallenge.bybit.ByBitAPI;
import com.profitchallenge.dto.PriceInfoDto;

import com.profitchallenge.indicators.CalculateIndicator;
import com.profitchallenge.repository.PriceInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceInfoService {
    private final ByBitAPI byBitAPI;
    private final CalculateIndicator calculateIndicator;
    private final PriceInfoRepository priceInfoRepository;
    private final Map<String,Object> socketMap = new HashMap<>();

    public PriceInfoDto getPriceInfo(String symbol, String minute) {
        return priceInfoRepository.findPriceInfo(minute+"_"+symbol,0,0).get(0);
    }

    public List<PriceInfoDto> getPriceInfoAll(String symbol, String minute) {
        return priceInfoRepository.findPriceInfo(minute+"_"+symbol,0,-1);
    }

    //200개의 분 캔들 정보를 저장
    @Transactional
    public void saveCandleInfo(String market,String minute) {
        try {
            String response = byBitAPI.getCandle(market,minute,"201");
            List<PriceInfoDto> priceInfoList = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode listNode = rootNode.path("result").path("list");
            JsonNode symbolNode = rootNode.path("result").path("symbol");

            for (JsonNode entry : listNode) {
                PriceInfoDto priceInfo = PriceInfoDto.builder()
                                        .symbol(symbolNode.asText())
                                        .tradeDate(convertTime(entry.get(0).asText()))
                                        .openingPrice(Double.parseDouble(entry.get(1).asText()))
                                        .highPrice(Double.parseDouble(entry.get(2).asText()))
                                        .lowPrice(Double.parseDouble(entry.get(3).asText()))
                                        .tradePrice(Double.parseDouble(entry.get(4).asText()))
                                        .tradeVolume(Double.parseDouble(entry.get(5).asText()))
                                        .build();

                priceInfoList.add(priceInfo);
            }

            if(priceInfoList.size()>60) {
                calculateIndicator.calculate(priceInfoList);
                priceInfoRepository.saveCandle(minute + "_" + market, priceInfoList);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public void savePriceInfo(String message, String minute) {
        PriceInfoDto priceInfoDto;
        String symbolKey;
        List<PriceInfoDto> priceInfoDtoList;

        try {
            priceInfoDto = messageConvert(message);
            symbolKey = minute+"_"+ priceInfoDto.getSymbol();
            priceInfoDtoList = priceInfoRepository.findPriceInfo(symbolKey,0,200);

            if (priceInfoDtoList.get(0).getTradeDate().equals(priceInfoDto.getTradeDate())) {
                priceInfoDtoList.set(0, priceInfoDto);

                if(priceInfoDtoList.size()>60) calculateIndicator.calculate(priceInfoDtoList);
                priceInfoRepository.updatePriceInfo(symbolKey, priceInfoDtoList.get(0));

            } else {
                priceInfoDtoList.add(0, priceInfoDto);
                if(priceInfoDtoList.size()>60) calculateIndicator.calculate(priceInfoDtoList);
                priceInfoRepository.insertPriceInfo(symbolKey, priceInfoDtoList.get(0));
            }
            //notificationService.findCondition(priceInfoDtoList,minute);

        } catch (Exception e) {
            log.error("savePriceInfo Exception: "+e.getMessage());
//            e.printStackTrace();
        }
    }

    //웹소켓 연결을 통해 코인에 대한 실시간 캔들 정보를 Redis에 저장
    public void connectWebSocket(String symbol, String minute) {
        WebSocket ws = null;
        JsonObject type = new JsonObject();
        JsonArray symbols = new JsonArray();
        symbols.add("kline."+minute+"."+symbol);

        type.addProperty("req_id", UUID.randomUUID().toString());
        type.addProperty("op","subscribe");
        type.add("args",symbols);
        priceInfoRepository.deletePriceInfo(symbol);
        saveCandleInfo(symbol,minute);
        try {
            ws = new WebSocketFactory()
                    .setConnectionTimeout(5000)
                    .createSocket("wss://stream.bybit.com/v5/public/linear")
                    .addListener(new WebSocketAdapter() {
                        public void onTextMessage(WebSocket websocket, String message) {
                            log.info(message);
                            if (!message.contains("success")) savePriceInfo(message,minute);
                        }
                        public void onDisconnected(WebSocket websocket,
                                                   WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                                   boolean closedByServer) {
                            log.info("onDisconnected : "+closedByServer);
                        }
                        public void onError(WebSocket websocket, WebSocketException cause) {
                            try {
                                connectWebSocket(symbol,minute);
                            } catch (Exception e) {
                                log.error(":onError"+e);
                            }
                            log.error("Error::"+cause.toString());
                        }
                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connect();
        } catch (WebSocketException | IOException e) {
            log.error("connectWebSocket Exception:" +e);
        }

        socketMap.put(symbol,ws);
        ws.sendText(type.toString());
    }

    public List<String> getConnectList() {
        return new ArrayList<>(socketMap.keySet());
    }

    public void disconnect(String symbol) {
        WebSocket webSocket = (WebSocket) socketMap.get(symbol);
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.disconnect();
            socketMap.remove(symbol);
            log.info("WebSocket with Symbol {} disconnected", symbol);
        }
    }

    //웹소켓을 통해 받아온 정보에 대해 변환
    private PriceInfoDto messageConvert(String message) throws JsonProcessingException {
        PriceInfoDto priceInfo = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(message);
        JsonNode dataNode = rootNode.path("data");
        String symbol = rootNode.get("topic").asText();

        if (dataNode.isArray() && dataNode.size() > 0) {
            JsonNode firstEntry = dataNode.get(0);
            priceInfo = PriceInfoDto.builder()
                    .symbol(symbol.substring(symbol.lastIndexOf(".")+1))
                    .tradeDate(convertTime(firstEntry.get("start").asText()))
                    .openingPrice(Double.parseDouble(firstEntry.get("open").asText()))
                    .highPrice(Double.parseDouble(firstEntry.get("high").asText()))
                    .lowPrice(Double.parseDouble(firstEntry.get("low").asText()))
                    .tradePrice(Double.parseDouble(firstEntry.get("close").asText()))
                    .tradeVolume(Double.parseDouble(firstEntry.get("volume").asText()))
                    .build();
        }
        return priceInfo;
    }

    //타임스탬프 변환
    private String convertTime(String timeStamp) {
        long timestamp = Long.parseLong(timeStamp);

        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return localDateTime.format(formatter);
    }
}
