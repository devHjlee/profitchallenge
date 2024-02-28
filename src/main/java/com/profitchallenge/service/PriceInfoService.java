package com.profitchallenge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.*;
import com.profitchallenge.dto.PriceInfoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceInfoService {
    private final Map<String,Object> socketMap = new HashMap<>();
    WebSocket ws = null;

    //웹소켓 연결을 통해 코인에 대한 실시간 시세를 Redis에 저장
    public void connectWebSocket(String symbol, String minute) throws Exception {
        log.info("connectWebSocket");
        JsonObject type = new JsonObject();
        JsonArray symbols = new JsonArray();
        symbols.add("kline."+minute+"."+symbol);

        type.addProperty("req_id", UUID.randomUUID().toString());
        type.addProperty("op","subscribe");
        type.add("args",symbols);

        String SERVER = "wss://stream.bybit.com/v5/public/linear";
        int TIMEOUT = 5000;
        ws = new WebSocketFactory()
                .setConnectionTimeout(TIMEOUT)
                .createSocket(SERVER)
                .addListener(new WebSocketAdapter() {

                    public void onBinaryMessage(WebSocket websocket, byte[] binary) {
                    }
                    public void onTextMessage(WebSocket websocket, String message) {
                        //if(!message.contains("success"))
                        log.info(message);
                    }
                    public void onDisconnected(WebSocket websocket,
                                               WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                               boolean closedByServer) {
                        try {
                            //log.info(symbolDto.getSymbol());
                            //priceConnect(symbolDto,minute);
                        } catch (Exception e) {
                            log.error(":disconnection"+e);
                        }
                        log.error(":disconnection");


                    }
                    public void onError(WebSocket websocket, WebSocketException cause) {
                        try {
                            //priceConnect(symbolDto,minute);
                        } catch (Exception e) {
                            log.error(":onError"+e);
                        }
                        log.error("Error::"+cause.toString());
                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();
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
            log.info("WebSocket with Symbol {} disconnected", symbol);
        } else {
            log.warn("WebSocket with Symbol {} not found or already disconnected", symbol);
        }
    }

    //웹소켓을 통해 받아온 정보에 대해 변환
    private PriceInfoDto getPriceInfo(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        PriceInfoDto priceInfo = new PriceInfoDto();
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode dataNode = rootNode.path("data");
            String symbol = rootNode.get("topic").asText();

            if (dataNode.isArray() && dataNode.size() > 0) {
                JsonNode firstEntry = dataNode.get(0);
                priceInfo.setTradeDate(convertTime(firstEntry.get("start").asText()));
                priceInfo.setSymbol(symbol.substring(symbol.lastIndexOf(".")+1));
                priceInfo.setTradePrice(Double.parseDouble(firstEntry.get("close").asText()));
                priceInfo.setOpeningPrice(Double.parseDouble(firstEntry.get("open").asText()));
                priceInfo.setHighPrice(Double.parseDouble(firstEntry.get("high").asText()));
                priceInfo.setLowPrice(Double.parseDouble(firstEntry.get("low").asText()));
                priceInfo.setTradeVolume(Double.parseDouble(firstEntry.get("volume").asText()));
            }

        } catch (JsonProcessingException e) {
            log.error("getPriceInfo JsonProcessingException:"+e.toString());
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
