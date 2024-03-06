package com.profitchallenge.bybit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;


@Slf4j
@Component
public class ByBitAPI {
    private final Map<String, String> apiConfig = readApiConfig();
    private final String API_KEY = apiConfig.get("api_key");
    private final String API_SECRET = apiConfig.get("api_secret");
    private final String RECV_WINDOW = "20000";

    //마진거래 가능한 코인목록
    public String getSymbols() throws IOException {
        String url = "https://api.bybit.com/v5/market/instruments-info?category=linear";
        return getRequest(url);
    }

    // 코인에 대한 캔들정보 수집
    public String getCandle(String market,String minute,String limit) throws IOException {
        String url = "https://api.bybit.com/v5/market/kline?category=inverse&symbol="+market+"&interval="+minute+"&limit="+limit;
        return getRequest(url);
    }

    //계정에 대한 잔고
    public String getAccount() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String url = "https://api.bybit.com/v5/account/wallet-balance";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("accountType", "CONTRACT");
        map.put("coin","USDT");

        return getRequest(url,timeStamp,map);
    }

    //계정에 대한 보유중인 코인에 대한 포지션
    public String getPosition() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String url = "https://api.bybit.com/v5/position/list";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("category", "linear");
        map.put("settleCoin","USDT");
        return getRequest(url,timeStamp,map);
    }

    //주문 전체 취소
    public String setCancelAllOrder() throws Exception {
        String url = "https://api.bybit.com/v5/order/cancel-all";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("category","linear");
        map.put("symbol", null);
        map.put("settleCoin", "USDT");
        map.put("timeInForce", "GTC");
        log.info(map.toString());
        return postRequest(url,timeStamp,map);
    }

    // POST: place a Linear perp order - contract v5
    public String setOrder(String market, String side, int positionIdx, String orderType, double qty, double price, double takeProfit, double stopLoss) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String url = "https://api.bybit.com/v5/order/create";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("category","linear");
        map.put("symbol", market);
        map.put("side", side);
        map.put("positionIdx", positionIdx);
        if("M".equals(orderType)) {
            map.put("orderType", "Market");
        }else {
            map.put("orderType", "Limit");
            map.put("price", String.valueOf(price));
        }

        map.put("qty", String.valueOf(qty));
        map.put("takeProfit",String.valueOf(takeProfit));
        map.put("stopLoss",String.valueOf(stopLoss));
        map.put("timeInForce", "GTC");
        log.info(map.toString());
        return postRequest(url,timeStamp,map);
    }

    //마진 레버리지 세팅
    public String setLeverage(String symbol, String buyLeverage, String sellLeverage) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String url = "https://api.bybit.com/v5/position/set-leverage";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("category","linear");
        map.put("symbol", symbol);
        map.put("buyLeverage",buyLeverage);
        map.put("sellLeverage",sellLeverage);
        map.put("timeInForce", "GTC");
        return postRequest(url,timeStamp,map);

    }

    //마진 격리,교차 설정 (격리 1, 교차 0)
    public String setSwitchIsolated(String symbol, int tradeMode, String buyLeverage, String sellLeverage) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String url = "https://api.bybit.com/v5/position/switch-isolated";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("category","linear");
        map.put("symbol", symbol);
        map.put("buyLeverage",buyLeverage);
        map.put("sellLeverage",sellLeverage);
        map.put("tradeMode", tradeMode);
        return postRequest(url,timeStamp,map);

    }

    //마진 양방향 단방향 설정(양방향 1, 단방향 0)
    public String setSwitchMode(String symbol, int mode) throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        String url = "https://api.bybit.com/v5/position/switch-mode";
        String timeStamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        Map<String, Object> map = new HashMap<>();
        map.put("category","linear");
        map.put("symbol", symbol);
        map.put("coin",null);
        map.put("mode",mode);
        return postRequest(url,timeStamp,map);
    }

    private String getRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

    private String getRequest(String url,String timeStamp, Map<String,Object> map) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String signature = genGetSign(map,timeStamp);
        StringBuilder sb = genQueryStr(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(url+"?"+sb)
                .get()
                .addHeader("X-BAPI-API-KEY", API_KEY)
                .addHeader("X-BAPI-SIGN", signature)
                .addHeader("X-BAPI-TIMESTAMP", timeStamp)
                .addHeader("X-BAPI-SIGN-TYPE", "2")
                .addHeader("X-BAPI-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

    private String postRequest(String url, String timeStamp, Map<String,Object> map) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Gson gson = new Gson();
        String signature = genPostSign(map,timeStamp);
        String jsonMap = gson.toJson(map);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonMap,mediaType))
                .addHeader("X-BAPI-API-KEY", API_KEY)
                .addHeader("X-BAPI-SIGN", signature)
                .addHeader("X-BAPI-SIGN-TYPE", "2")
                .addHeader("X-BAPI-TIMESTAMP", timeStamp)
                .addHeader("X-BAPI-RECV-WINDOW", RECV_WINDOW)
                .addHeader("Content-Type", "application/json")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        return Objects.requireNonNull(response.body()).string();
    }

    private String genGetSign(Map<String, Object> params, String timeStamp) throws NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder sb = genQueryStr(params);
        String queryStr = timeStamp + API_KEY + RECV_WINDOW + sb;

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(API_SECRET.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return bytesToHex(sha256_HMAC.doFinal(queryStr.getBytes()));
    }

    /**
     * To convert bytes to hex
     * @param hash
     * @return hex string
     */
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * To generate query string for GET requests
     * @param map
     * @return
     */
    private StringBuilder genQueryStr(Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            String key = iter.next();
            sb.append(key)
                    .append("=")
                    .append(map.get(key))
                    .append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb;
    }

    /**
     * The way to generate the sign for POST requests
     * @param params: Map input parameters
     * @return signature used to be a parameter in the header
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private String genPostSign(Map<String, Object> params, String timeStamp) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(API_SECRET.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        Gson gson = new Gson();
        String paramJson = gson.toJson(params);
        String sb = timeStamp + API_KEY + RECV_WINDOW + paramJson;
        return bytesToHex(sha256_HMAC.doFinal(sb.getBytes()));
    }

    private Map<String, String> readApiConfig() {
        Map<String, String> apiConfig = new HashMap<>();
        try {

            //Path path = Paths.get("D:\\dev\\api.json");
            Path path = Paths.get("/tmp/api.json");
            byte[] jsonData = Files.readAllBytes(path);
            ObjectMapper objectMapper = new ObjectMapper();
            apiConfig = objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiConfig;
    }
}
