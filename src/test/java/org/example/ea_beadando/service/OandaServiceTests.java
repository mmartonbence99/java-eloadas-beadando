package org.example.ea_beadando.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OandaServiceTests {

    @Test
    void rejectsMissingToken() {
        OandaService service = new OandaService("https://api-fxpractice.oanda.com", "", "account");
        IllegalStateException error = assertThrows(IllegalStateException.class, service::getAccount);
        assertEquals("Az OANDA_TOKEN és az OANDA_ACCOUNT_ID környezeti változókat be kell állítani.", error.getMessage());
    }

    @Test
    void rejectsMissingTokenForTradeOperations() {
        // Token nélkül a kereskedési műveletek hálózati hívás nélkül, azonnal hibát dobnak.
        OandaService service = new OandaService("https://api-fxpractice.oanda.com", "", " ");
        assertThrows(IllegalStateException.class, () -> service.openTrade("EUR_USD", 1));
        assertThrows(IllegalStateException.class, () -> service.closeTrade("1"));
    }
}