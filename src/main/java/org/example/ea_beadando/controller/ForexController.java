package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForexController {

    private final OandaService oanda;

    public ForexController(OandaService oanda) {
        this.oanda = oanda;
    }

    @GetMapping("/forex-account")
    public String account(Model model) {
        model.addAttribute("active", "forex-account");
        model.addAttribute("title", "Forex-account");
        try {
            model.addAttribute("account", oanda.getAccount());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forex-account";
    }
}
