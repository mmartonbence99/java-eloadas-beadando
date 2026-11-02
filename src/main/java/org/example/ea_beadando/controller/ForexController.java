package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/forex-aktar")
    public String aktar(@RequestParam(required = false) String instrument, Model model) {
        model.addAttribute("active", "forex-aktar");
        model.addAttribute("title", "Forex-AktÁr");
        try {
            model.addAttribute("instruments", oanda.getInstruments());
            if (instrument != null) {
                model.addAttribute("price", oanda.getPrice(instrument));
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("instrument", instrument);
        return "forex-aktar";
    }
}
