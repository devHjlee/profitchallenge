package com.profitchallenge.service;

import com.neovisionaries.ws.client.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriceInfoServiceTest {

    private PriceInfoService priceInfoService;

    @Mock
    private WebSocketFactory webSocketFactory;

    @Mock
    private WebSocket webSocket;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        priceInfoService = new PriceInfoService();

        // WebSocketFactory의 createSocket 메소드가 호출될 때 목(mock) 객체인 webSocket을 반환하도록 설정
        when(webSocketFactory.createSocket(anyString())).thenReturn(webSocket);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void connectWebSocket() throws Exception {
        // 테스트할 기호(symbol)와 분(minute)
        String symbol = "BTCUSD";
        String minute = "1";

        // 테스트할 WebSocket의 URL
        String SERVER = "wss://stream.bybit.com/v5/public/linear";

        // WebSocketFactory의 createSocket 메소드가 특정 URL을 인자로 호출되었을 때 목(mock) 객체인 webSocket을 반환하도록 설정
        when(webSocketFactory.createSocket(SERVER)).thenReturn(webSocket);

        // connectWebSocket 메소드 호출
        priceInfoService.connectWebSocket(symbol, minute);
        priceInfoService.disconnect(symbol);

        // WebSocketFactory의 createSocket 메소드가 특정 URL을 인자로 호출되었는지 검증
        //verify(webSocketFactory, times(1)).createSocket(SERVER);

        // WebSocket의 sendText 메소드가 호출되었는지 검증
        //verify(webSocket, times(1)).sendText(anyString());
    }

    @Test
    void disconnect() {
        // 테스트할 기호(symbol)
        String symbol = "BTCUSD";

        // disconnect 메소드 호출
        priceInfoService.disconnect(symbol);

        // WebSocket의 disconnect 메소드가 호출되었는지 검증
        //verify(webSocket, times(1)).disconnect();
    }
}
