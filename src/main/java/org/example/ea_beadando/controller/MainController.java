package org.example.ea_beadando.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/forex-account")
    public String forexAccount(Model model) {
        return page(model, "forex-account", "Forex-account");
    }

    @GetMapping("/forex-aktar")
    public String forexAktar(Model model) {
        return page(model, "forex-aktar", "Forex-AktÁr");
    }

    @GetMapping("/forex-histar")
    public String forexHistar(Model model) {
        return page(model, "forex-histar", "Forex-HistÁr");
    }

    @GetMapping("/forex-nyit")
    public String forexNyit(Model model) {
        return page(model, "forex-nyit", "Forex-Nyit");
    }

    @GetMapping("/forex-poz")
    public String forexPoz(Model model) {
        return page(model, "forex-poz", "Forex-Poz");
    }

    @GetMapping("/forex-zar")
    public String forexZar(Model model) {
        return page(model, "forex-zar", "Forex-Zár");
    }

    private String page(Model model, String active, String title) {
        model.addAttribute("active", active);
        model.addAttribute("title", title);
        return "page";
    }
}
