package org.example.ea_beadando.service;

import hu.mnb.soap.MNBArfolyamServiceSoap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MnbServiceTests {

    @Test
    void normalizesRatesToOneCurrencyUnit() throws Exception {
        MNBArfolyamServiceSoap port = mock(MNBArfolyamServiceSoap.class);
        when(port.getExchangeRates("2026-01-01", "2026-01-02", "JPY")).thenReturn("""
                <MNBExchangeRates>
                  <Day date="2026-01-01"><Rate unit="100" curr="JPY">243,50</Rate></Day>
                  <Day date="2026-01-02"><Rate unit="1" curr="JPY">2,5</Rate></Day>
                </MNBExchangeRates>
                """);
        MnbService service = new MnbService(() -> port);

        assertEquals(List.of(new MnbService.Rate("2026-01-01", 2.435),
                        new MnbService.Rate("2026-01-02", 2.5)),
                service.getRates("2026-01-01", "2026-01-02", "JPY"));
    }

    @Test
    void returnsEmptyListWhenNoRatesExist() throws Exception {
        MNBArfolyamServiceSoap port = mock(MNBArfolyamServiceSoap.class);
        when(port.getExchangeRates("2026-01-01", "2026-01-01", "EUR"))
                .thenReturn("<MNBExchangeRates/>");

        assertEquals(List.of(), new MnbService(() -> port).getRates("2026-01-01", "2026-01-01", "EUR"));
    }

    @Test
    void rejectsInvalidCurrencyUnit() throws Exception {
        MNBArfolyamServiceSoap port = mock(MNBArfolyamServiceSoap.class);
        when(port.getExchangeRates("2026-01-01", "2026-01-01", "EUR"))
                .thenReturn("<MNBExchangeRates><Day date=\"2026-01-01\"><Rate unit=\"0\">1</Rate></Day></MNBExchangeRates>");

        assertThrows(IllegalArgumentException.class,
                () -> new MnbService(() -> port).getRates("2026-01-01", "2026-01-01", "EUR"));
    }
}
