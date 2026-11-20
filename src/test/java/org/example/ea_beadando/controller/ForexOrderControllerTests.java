package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.OandaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ForexOrderControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OandaService oanda;

    @BeforeEach
    void setUp() {
        when(oanda.getInstruments()).thenReturn(List.of("EUR_USD", "USD_JPY"));
    }

    @Test
    void opensLongAndShortOrders() throws Exception {
        when(oanda.openTrade("EUR_USD", 10)).thenReturn(Map.of("orderFillTransaction", Map.of("id", "1")));
        when(oanda.openTrade("USD_JPY", -10)).thenReturn(Map.of("orderFillTransaction", Map.of("id", "2")));

        mvc.perform(post("/forex-nyit").param("instrument", "EUR_USD").param("units", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("instrument", "EUR_USD"))
                .andExpect(model().attribute("units", "10"))
                .andExpect(content().string(containsString("Sikeres megbízás")));
        mvc.perform(post("/forex-nyit").param("instrument", "USD_JPY").param("units", "-10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sikeres megbízás")));
        verify(oanda).openTrade("EUR_USD", 10);
        verify(oanda).openTrade("USD_JPY", -10);
    }

    @Test
    void rejectsZeroAndNonIntegerUnitsWithoutOrdering() throws Exception {
        mvc.perform(post("/forex-nyit").param("instrument", "EUR_USD").param("units", "0"))
                .andExpect(model().attribute("error", "A mennyiség nem lehet nulla."))
                .andExpect(model().attribute("units", "0"));
        mvc.perform(post("/forex-nyit").param("instrument", "EUR_USD").param("units", "1.5"))
                .andExpect(model().attribute("error", "Adjon meg érvényes egész mennyiséget."));
        mvc.perform(post("/forex-nyit").param("instrument", "EUR_USD"))
                .andExpect(model().attribute("error", "Adjon meg érvényes egész mennyiséget."));
        verify(oanda, never()).openTrade(anyString(), anyInt());
    }

    @Test
    void rejectsUnknownInstrumentWithoutOrdering() throws Exception {
        mvc.perform(post("/forex-nyit").param("instrument", "INVALID").param("units", "1"))
                .andExpect(model().attribute("error", "Válasszon érvényes instrumentumot."));
        verify(oanda, never()).openTrade(anyString(), anyInt());
    }

    @Test
    void reportsRejectedOrder() throws Exception {
        when(oanda.openTrade("EUR_USD", 1))
                .thenReturn(Map.of("orderCancelTransaction", Map.of("reason", "MARKET_HALTED")));
        mvc.perform(post("/forex-nyit").param("instrument", "EUR_USD").param("units", "1"))
                .andExpect(model().attribute("error", "A megbízás nem teljesült. Ok: MARKET_HALTED"))
                .andExpect(model().attributeDoesNotExist("message"));
    }

    @Test
    void closesValidTradeAndPreservesId() throws Exception {
        when(oanda.closeTrade("123")).thenReturn(Map.of("orderFillTransaction", Map.of("id", "4")));
        mvc.perform(post("/forex-zar").param("tradeId", "123"))
                .andExpect(model().attribute("tradeId", "123"))
                .andExpect(content().string(containsString("Sikeres zárás")));
        verify(oanda).closeTrade("123");
    }

    @Test
    void rejectsInvalidTradeIdsWithoutClosing() throws Exception {
        for (String id : List.of("", "0", "000", "-1", "1.5", "abc")) {
            mvc.perform(post("/forex-zar").param("tradeId", id))
                    .andExpect(model().attribute("error", "Adjon meg pozitív egész trade azonosítót."));
        }
        verify(oanda, never()).closeTrade(anyString());
    }

    @Test
    void reportsInstrumentLookupFailureWithoutOrdering() throws Exception {
        when(oanda.getInstruments()).thenThrow(new IllegalStateException("Az instrumentumok nem érhetők el."));
        mvc.perform(post("/forex-nyit").param("instrument", "EUR_USD").param("units", "1"))
                .andExpect(model().attribute("error", "Az instrumentumok nem érhetők el."));
        verify(oanda, never()).openTrade(anyString(), anyInt());
    }
}
