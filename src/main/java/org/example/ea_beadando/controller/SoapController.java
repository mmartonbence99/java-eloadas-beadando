package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.MnbService;
import org.example.ea_beadando.service.MnbService.Rate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SoapController {

    private final MnbService mnb;

    public SoapController(MnbService mnb) {
        this.mnb = mnb;
    }

    @GetMapping("/soap")
    public String soap(@RequestParam(required = false) String currency,
                       @RequestParam(required = false) String start,
                       @RequestParam(required = false) String end,
                       Model model) {
        model.addAttribute("active", "soap");
        model.addAttribute("title", "SOAP");
        try {
            model.addAttribute("currencies", mnb.getCurrencies());
            if (currency == null) {
                currency = "EUR";
                end = LocalDate.now().toString();
                start = LocalDate.now().minusDays(30).toString();
            } else {
                List<Rate> rates = mnb.getRates(start, end, currency);
                model.addAttribute("rates", rates);
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("currency", currency);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "soap";
    }
}
