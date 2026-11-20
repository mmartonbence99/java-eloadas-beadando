package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.MnbService;
import org.example.ea_beadando.service.MnbService.Rate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        boolean initialRequest = currency == null && start == null && end == null;
        if (initialRequest) {
            currency = "EUR";
            end = LocalDate.now().toString();
            start = LocalDate.now().minusDays(30).toString();
        }
        try {
            List<String> currencies = mnb.getCurrencies();
            model.addAttribute("currencies", currencies);
            if (!initialRequest) {
                if (currency == null || !currencies.contains(currency)) {
                    throw new IllegalArgumentException("Válasszon érvényes devizát.");
                }
                if (start == null || end == null) {
                    throw new IllegalArgumentException("Adja meg a kezdő és a befejező dátumot.");
                }
                LocalDate startDate = LocalDate.parse(start);
                LocalDate endDate = LocalDate.parse(end);
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("A kezdő dátum nem lehet később a befejező dátumnál.");
                }
                List<Rate> rates = mnb.getRates(start, end, currency);
                model.addAttribute("rates", rates);
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Adjon meg érvényes dátumokat.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("currency", currency);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "soap";
    }
}
