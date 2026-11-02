package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/forex-histar")
    public String histar(@RequestParam(required = false) String instrument,
                         @RequestParam(required = false) String granularity,
                         Model model) {
        model.addAttribute("active", "forex-histar");
        model.addAttribute("title", "Forex-HistÁr");
        model.addAttribute("granularities", List.of("S5", "S30", "M1", "M5", "M15", "M30", "H1", "H4", "D", "W", "M"));
        try {
            model.addAttribute("instruments", oanda.getInstruments());
            if (instrument != null && granularity != null) {
                model.addAttribute("candles", oanda.getCandles(instrument, granularity));
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("instrument", instrument);
        model.addAttribute("granularity", granularity);
        return "forex-histar";
    }

    @GetMapping("/forex-nyit")
    public String nyit(Model model) {
        model.addAttribute("active", "forex-nyit");
        model.addAttribute("title", "Forex-Nyit");
        try {
            model.addAttribute("instruments", oanda.getInstruments());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forex-nyit";
    }

    @PostMapping("/forex-nyit")
    public String nyitPost(@RequestParam String instrument, @RequestParam int units, Model model) {
        try {
            Map<String, Object> result = oanda.openTrade(instrument, units);
            model.addAttribute("message", "Sikeres megbízás: " + result.get("orderFillTransaction"));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return nyit(model);
    }

    @GetMapping("/forex-poz")
    public String poz(Model model) {
        model.addAttribute("active", "forex-poz");
        model.addAttribute("title", "Forex-Poz");
        try {
            model.addAttribute("trades", oanda.getOpenTrades());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forex-poz";
    }

    @GetMapping("/forex-zar")
    public String zar(Model model) {
        model.addAttribute("active", "forex-zar");
        model.addAttribute("title", "Forex-Zár");
        return "forex-zar";
    }

    @PostMapping("/forex-zar")
    public String zarPost(@RequestParam String tradeId, Model model) {
        try {
            Map<String, Object> result = oanda.closeTrade(tradeId);
            model.addAttribute("message", "Sikeres zárás: " + result.get("orderFillTransaction"));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return zar(model);
    }
}
