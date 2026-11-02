package org.example.ea_beadando.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OandaService {

    private final RestClient client;
    private final String accountId;

    public OandaService(@Value("${oanda.url}") String url,
                        @Value("${oanda.token}") String token,
                        @Value("${oanda.account-id}") String accountId) {
        this.accountId = accountId;
        this.client = RestClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    public Map<String, Object> getAccount() {
        Map<String, Object> response = client.get()
                .uri("/v3/accounts/{id}/summary", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        Map<String, Object> account = (Map<String, Object>) response.get("account");
        return account;
    }

    public List<String> getInstruments() {
        Map<String, Object> response = client.get()
                .uri("/v3/accounts/{id}/instruments", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("instruments");
        return list.stream().map(i -> (String) i.get("name")).sorted().toList();
    }

    public Map<String, Object> getPrice(String instrument) {
        Map<String, Object> response = client.get()
                .uri("/v3/accounts/{id}/pricing?instruments={i}", accountId, instrument)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> prices = (List<Map<String, Object>>) response.get("prices");
        return prices.get(0);
    }

    public List<Map<String, Object>> getCandles(String instrument, String granularity) {
        Map<String, Object> response = client.get()
                .uri("/v3/instruments/{i}/candles?count=10&granularity={g}&price=M", instrument, granularity)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candles = (List<Map<String, Object>>) response.get("candles");
        return candles;
    }

    public Map<String, Object> openTrade(String instrument, int units) {
        Map<String, Object> order = Map.of("order", Map.of(
                "type", "MARKET",
                "instrument", instrument,
                "units", String.valueOf(units),
                "timeInForce", "FOK",
                "positionFill", "DEFAULT"));
        return client.post()
                .uri("/v3/accounts/{id}/orders", accountId)
                .body(order)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<Map<String, Object>> getOpenTrades() {
        Map<String, Object> response = client.get()
                .uri("/v3/accounts/{id}/openTrades", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trades = (List<Map<String, Object>>) response.get("trades");
        return trades;
    }

    public Map<String, Object> closeTrade(String tradeId) {
        return client.put()
                .uri("/v3/accounts/{id}/trades/{t}/close", accountId, tradeId)
                .body(Map.of("units", "ALL"))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
