package com.profitchallenge.service;

import com.profitchallenge.websocket.NvWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceInfoService {
    private final NvWebSocket nvWebSocket;

    //웹소켓 연결을 통해 코인에 대한 실시간 시세를 Redis에 저장
    public void setNvWebSocket(String symbol) throws Exception {
        nvWebSocket.setNvWebSocket(symbol,"1");
    }
}
