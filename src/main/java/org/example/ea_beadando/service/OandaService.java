package org.example.ea_beadando.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OandaService {

    private final String url;
    private volatile Credentials credentials;

    private static class Credentials {
        private final RestClient client;
        private final String token;
        private volatile String accountId;

        private Credentials(String url, String token, String accountId) {
            this.token = token;
            this.accountId = accountId;
            this.client = RestClient.builder()
                    .baseUrl(url)
                    .defaultHeader("Authorization", "Bearer " + token)
                    .build();
        }
    }

    public OandaService(@Value("${oanda.url}") String url,
                        @Value("${oanda.token}") String token,
                        @Value("${oanda.account-id:}") String accountId) {
        this.url = url;
        this.credentials = new Credentials(url, token, accountId);
    }

    public void configure(String token, String accountId) {
        String cleanedToken = token.trim();
        String cleanedAccountId = accountId.trim();
        if (cleanedToken.isEmpty() || cleanedToken.length() > 512
                || !cleanedToken.matches("[A-Za-z0-9._~-]+")) {
            throw new IllegalArgumentException("Adjon meg érvényes Oanda v20 API-tokent.");
        }
        if (cleanedAccountId.length() > 128 || !cleanedAccountId.matches("[A-Za-z0-9_-]*")) {
            throw new IllegalArgumentException("Adjon meg érvényes számlaazonosítót.");
        }
        credentials = new Credentials(url, cleanedToken, cleanedAccountId);
    }

    public boolean isConfigured() {
        return !credentials.token.isBlank();
    }

    private Credentials credentials() {
        Credentials current = credentials;
        if (current.token.isBlank()) {
            throw new IllegalStateException("Adja meg az Oanda API-tokent az Oanda-beállítások oldalon.");
        }
        return current;
    }

    // Ha nincs megadva számlaazonosító, a tokenhez tartozó számlát az OANDA-tól kérdezzük le (GET /v3/accounts).
    private String accountId(Credentials current) {
        if (current.accountId == null || current.accountId.isBlank()) {
            Map<String, Object> response = current.client.get()
                    .uri("/v3/accounts")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> accounts = (List<Map<String, Object>>) response.get("accounts");
            if (accounts == null || accounts.isEmpty()) {
                throw new IllegalStateException("A tokenhez nem tartozik OANDA számla.");
            }
            current.accountId = (String) accounts.get(0).get("id");
        }
        return current.accountId;
    }

    public Map<String, Object> getAccount() {
        Credentials current = credentials();
        Map<String, Object> response = current.client.get()
                .uri("/v3/accounts/{id}/summary", accountId(current))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        Map<String, Object> account = (Map<String, Object>) response.get("account");
        return account;
    }

    public List<String> getInstruments() {
        Credentials current = credentials();
        Map<String, Object> response = current.client.get()
                .uri("/v3/accounts/{id}/instruments", accountId(current))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("instruments");
        return list.stream().map(i -> (String) i.get("name")).sorted().toList();
    }

    public Map<String, Object> getPrice(String instrument) {
        Credentials current = credentials();
        Map<String, Object> response = current.client.get()
                .uri("/v3/accounts/{id}/pricing?instruments={i}", accountId(current), instrument)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> prices = (List<Map<String, Object>>) response.get("prices");
        return prices.get(0);
    }

    public List<Map<String, Object>> getCandles(String instrument, String granularity) {
        Credentials current = credentials();
        Map<String, Object> response = current.client.get()
                .uri("/v3/instruments/{i}/candles?count=10&granularity={g}&price=M", instrument, granularity)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candles = (List<Map<String, Object>>) response.get("candles");
        return candles;
    }

    public Map<String, Object> openTrade(String instrument, int units) {
        Credentials current = credentials();
        Map<String, Object> order = Map.of("order", Map.of(
                "type", "MARKET",
                "instrument", instrument,
                "units", String.valueOf(units),
                "timeInForce", "FOK",
                "positionFill", "DEFAULT"));
        return current.client.post()
                .uri("/v3/accounts/{id}/orders", accountId(current))
                .body(order)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<Map<String, Object>> getOpenTrades() {
        Credentials current = credentials();
        Map<String, Object> response = current.client.get()
                .uri("/v3/accounts/{id}/openTrades", accountId(current))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trades = (List<Map<String, Object>>) response.get("trades");
        return trades;
    }

    public Map<String, Object> closeTrade(String tradeId) {
        Credentials current = credentials();
        return current.client.put()
                .uri("/v3/accounts/{id}/trades/{t}/close", accountId(current), tradeId)
                .body(Map.of("units", "ALL"))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}