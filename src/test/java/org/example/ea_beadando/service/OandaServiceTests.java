package org.example.ea_beadando.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OandaServiceTests {

    @Test
    void rejectsMissingToken() {
        OandaService service = new OandaService("https://api-fxpractice.oanda.com", "", "account");
        IllegalStateException error = assertThrows(IllegalStateException.class, service::getAccount);
        assertEquals("Adja meg az Oanda API-tokent az Oanda-beállítások oldalon.", error.getMessage());
    }

    @Test
    void rejectsMissingTokenForTradeOperations() {
        // Token nélkül a kereskedési műveletek hálózati hívás nélkül, azonnal hibát dobnak.
        OandaService service = new OandaService("https://api-fxpractice.oanda.com", "", " ");
        assertThrows(IllegalStateException.class, () -> service.openTrade("EUR_USD", 1));
        assertThrows(IllegalStateException.class, () -> service.closeTrade("1"));
    }

    @Test
    void discoversAccountFromTokenAndReusesIt() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger discoveries = new AtomicInteger();
        server.createContext("/v3/accounts", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String body;
            if (path.equals("/v3/accounts")) {
                discoveries.incrementAndGet();
                body = "{\"accounts\":[{\"id\":\"demo-account\"}]}";
            } else if (path.equals("/v3/accounts/demo-account/summary")) {
                body = "{\"account\":{\"currency\":\"USD\"}}";
            } else {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            byte[] data = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, data.length);
            try (var output = exchange.getResponseBody()) {
                output.write(data);
            }
        });
        server.start();
        try {
            OandaService service = new OandaService("http://127.0.0.1:" + server.getAddress().getPort(),
                    "test-token", "");
            assertEquals("USD", service.getAccount().get("currency"));
            assertEquals("USD", service.getAccount().get("currency"));
            assertEquals(1, discoveries.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void changingCredentialsUsesNewTokenAndAccount() throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/v3/accounts", exchange -> {
                String token = exchange.getRequestHeaders().getFirst("Authorization");
                String path = exchange.getRequestURI().getPath();
                String body;
                if (path.equals("/v3/accounts") && "Bearer first-token".equals(token)) {
                    body = "{\"accounts\":[{\"id\":\"first-account\"}]}";
                } else if (path.equals("/v3/accounts/first-account/summary") && "Bearer first-token".equals(token)) {
                    body = "{\"account\":{\"currency\":\"USD\"}}";
                } else if (path.equals("/v3/accounts/second-account/summary") && "Bearer second-token".equals(token)) {
                    body = "{\"account\":{\"currency\":\"EUR\"}}";
                } else {
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }
                byte[] data = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, data.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(data);
                }
            });
            server.start();
            try {
                OandaService service = new OandaService("http://127.0.0.1:" + server.getAddress().getPort(), "", "");
                service.configure("first-token", "");
                assertTrue(service.isConfigured());
                assertEquals("USD", service.getAccount().get("currency"));
                assertThrows(IllegalArgumentException.class, () -> service.configure("bad\nvalue", ""));
                assertEquals("USD", service.getAccount().get("currency"));
                service.configure("second-token", "second-account");
                assertEquals("EUR", service.getAccount().get("currency"));
            } finally {
                server.stop(0);
        }
    }
}