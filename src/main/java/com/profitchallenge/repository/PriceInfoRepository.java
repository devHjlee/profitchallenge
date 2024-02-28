package com.profitchallenge.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profitchallenge.dto.PriceInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PriceInfoRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveCandle(String symbol, List<PriceInfoDto> priceInfoDtoList) throws JsonProcessingException{
        for(PriceInfoDto priceInfoDto : priceInfoDtoList) {
            redisTemplate.opsForList().rightPush(symbol, objectMapper.writeValueAsString(priceInfoDto));
        }
    }

    public List<PriceInfoDto> findPriceInfo(String symbol,int startIdx, int endIdx) {
        List<Object> result = redisTemplate.opsForList().range(symbol,startIdx,endIdx);
        return result.stream().map(item -> {
                    try {
                        return objectMapper.readValue((String)item,PriceInfoDto.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    public void updatePriceInfo(String symbol, PriceInfoDto priceInfoDto) throws JsonProcessingException {
        redisTemplate.opsForList().set(symbol,0, objectMapper.writeValueAsString(priceInfoDto));
    }

    public void insertPriceInfo(String symbol, PriceInfoDto priceInfoDto) throws JsonProcessingException {
        redisTemplate.opsForList().leftPush(symbol, objectMapper.writeValueAsString(priceInfoDto));
    }

    public void deletePriceInfo(String symbol) {
        redisTemplate.delete(symbol);
    }
}
