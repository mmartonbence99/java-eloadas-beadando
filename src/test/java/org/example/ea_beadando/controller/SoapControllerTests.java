package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.MnbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SoapControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MnbService mnb;

    @BeforeEach
    void setUp() throws Exception {
        when(mnb.getCurrencies()).thenReturn(List.of("EUR", "JPY"));
    }

    @Test
    void showsInitialFormWithoutFetchingRates() throws Exception {
        mvc.perform(get("/soap"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currency", "EUR"))
                .andExpect(model().attributeDoesNotExist("rates"));
        verify(mnb, never()).getRates(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void showsRatesInTableAndChart() throws Exception {
        when(mnb.getRates("2026-01-01", "2026-01-02", "JPY"))
                .thenReturn(List.of(new MnbService.Rate("2026-01-02", 2.435)));
        mvc.perform(get("/soap").param("currency", "JPY").param("start", "2026-01-01").param("end", "2026-01-02"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2.435")))
                .andExpect(content().string(containsString("HUF / 1 devizaegység")))
                .andExpect(content().string(containsString("#2c6e8f")));
    }

    @Test
    void rejectsReversedDates() throws Exception {
        mvc.perform(get("/soap").param("currency", "EUR").param("start", "2026-01-02").param("end", "2026-01-01"))
                .andExpect(model().attribute("error", "A kezdő dátum nem lehet később a befejező dátumnál."))
                .andExpect(model().attributeDoesNotExist("rates"));
        verify(mnb, never()).getRates(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsInvalidDateAndCurrency() throws Exception {
        mvc.perform(get("/soap").param("currency", "EUR").param("start", "invalid").param("end", "2026-01-01"))
                .andExpect(model().attribute("error", "Adjon meg érvényes dátumokat."));
        mvc.perform(get("/soap").param("currency", "XXX").param("start", "2026-01-01").param("end", "2026-01-01"))
                .andExpect(model().attribute("error", "Válasszon érvényes devizát."));
    }

    @Test
    void rejectsMissingDates() throws Exception {
        mvc.perform(get("/soap").param("currency", "EUR"))
                .andExpect(model().attribute("error", "Adja meg a kezdő és a befejező dátumot."));
    }
}
