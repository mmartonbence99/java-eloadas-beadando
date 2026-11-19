package org.example.ea_beadando.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerTests {
    @Autowired
    private MockMvc mvc;

    @Test
    void showsCalculator() throws Exception {
        mvc.perform(get("/teszt"))
                .andExpect(status().isOk())
                .andExpect(view().name("teszt"))
                .andExpect(model().attribute("operation", "add"))
                .andExpect(content().string(containsString("Számológép")))
                .andExpect(content().string(containsString("href=\"/teszt\"")));
    }

    @Test
    void calculatesAndPreservesInputs() throws Exception {
        mvc.perform(post("/teszt").param("left", "5.5").param("right", "2").param("operation", "divide"))
                .andExpect(status().isOk())
                .andExpect(view().name("teszt"))
                .andExpect(model().attribute("left", "5.5"))
                .andExpect(model().attribute("right", "2"))
                .andExpect(model().attribute("operation", "divide"))
                .andExpect(model().attribute("result", "2.75"))
                .andExpect(content().string(containsString(">2.75</strong>")));
    }

    @Test
    void reportsDivisionByZero() throws Exception {
        mvc.perform(post("/teszt").param("left", "5").param("right", "0").param("operation", "divide"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Nullával nem lehet osztani."))
                .andExpect(model().attributeDoesNotExist("result"));
    }

    @Test
    void reportsInvalidOrMissingNumbers() throws Exception {
        mvc.perform(post("/teszt").param("left", "abc").param("operation", "add"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Mindkét mezőben érvényes számot adjon meg."))
                .andExpect(model().attribute("left", "abc"))
                .andExpect(model().attribute("right", ""))
                .andExpect(model().attributeDoesNotExist("result"));
    }

    @Test
    void reportsInvalidOperation() throws Exception {
        mvc.perform(post("/teszt").param("left", "1").param("right", "2").param("operation", "unknown"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Érvénytelen művelet."))
                .andExpect(model().attributeDoesNotExist("result"));
    }
}
