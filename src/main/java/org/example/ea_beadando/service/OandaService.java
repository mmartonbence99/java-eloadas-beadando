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
}
