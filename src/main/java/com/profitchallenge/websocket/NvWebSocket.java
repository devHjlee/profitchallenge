package com.profitchallenge.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class NvWebSocket {
    private static final String SERVER = "wss://stream.bybit.com/v5/public/linear";
    private static final int TIMEOUT = 5000;
    private Map<String,Object> socketMap = new HashMap<>();
    WebSocket ws = null;

    //웹소켓 연결을 통해 코인에 대한 실시간 시세를 Redis에 저장
    public void setNvWebSocket(String symbol, String minute) throws Exception {
        log.info("tradeConnect");
        JsonObject type = new JsonObject();
        JsonArray symbols = new JsonArray();
        symbols.add("kline."+minute+"."+symbol);

        type.addProperty("req_id",UUID.randomUUID().toString());
        type.addProperty("op","subscribe");
        type.add("args",symbols);

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

    public List<String> getNvWebSocket() {
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
}