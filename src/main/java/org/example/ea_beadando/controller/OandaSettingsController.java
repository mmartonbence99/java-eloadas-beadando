package org.example.ea_beadando.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.ea_beadando.service.OandaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Controller
public class OandaSettingsController {

    private static final String CSRF_ATTRIBUTE = OandaSettingsController.class.getName() + ".csrf";
    private final OandaService oanda;
    private final SecureRandom random = new SecureRandom();

    public OandaSettingsController(OandaService oanda) {
        this.oanda = oanda;
    }

    @GetMapping("/oanda-beallitasok")
    public String settings(HttpSession session, HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        String csrf = (String) session.getAttribute(CSRF_ATTRIBUTE);
        if (csrf == null) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            csrf = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(CSRF_ATTRIBUTE, csrf);
        }
        model.addAttribute("csrf", csrf);
        model.addAttribute("configured", oanda.isConfigured());
        return "oanda-beallitasok";
    }

    @PostMapping("/oanda-beallitasok")
    public String save(@RequestParam(defaultValue = "") String token,
                       @RequestParam(defaultValue = "") String accountId,
                       @RequestParam(defaultValue = "") String csrf,
                       HttpSession session, HttpServletResponse response,
                       RedirectAttributes redirect) {
        response.setHeader("Cache-Control", "no-store");
        String expected = (String) session.getAttribute(CSRF_ATTRIBUTE);
        if (expected == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                csrf.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Érvénytelen űrlap.");
        }
        try {
            oanda.configure(token, accountId);
            redirect.addFlashAttribute("message",
                    "Az adatokat eltároltuk a program futásának idejére. A kapcsolatot a Forex-account oldalon ellenőrizheti.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/oanda-beallitasok";
    }
}
