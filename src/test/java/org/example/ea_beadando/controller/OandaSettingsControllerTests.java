package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.OandaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OandaSettingsControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OandaService oanda;

    @Test
    void savesCredentialsOnlyWithSessionTokenAndDoesNotEchoThem() throws Exception {
        mvc.perform(post("/oanda-beallitasok").param("token", "private-token"))
                .andExpect(status().isForbidden());
        verify(oanda, never()).configure("private-token", "");

        MvcResult page = mvc.perform(get("/oanda-beallitasok"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) page.getRequest().getSession(false);
        String csrf = (String) page.getModelAndView().getModel().get("csrf");
        mvc.perform(post("/oanda-beallitasok").session(session)
                        .param("csrf", csrf).param("token", "private-token").param("accountId", "account-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oanda-beallitasok"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(not(containsString("private-token"))));
        verify(oanda).configure("private-token", "account-1");
    }
}
