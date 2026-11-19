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
    void rejectsMissingAccountForTradeOperations() {
        OandaService service = new OandaService("https://api-fxpractice.oanda.com", "token", " ");
        assertThrows(IllegalStateException.class, () -> service.openTrade("EUR_USD", 1));
        assertThrows(IllegalStateException.class, () -> service.closeTrade("1"));
    }
}
