package org.example.ea_beadando.controller;

import org.example.ea_beadando.service.CalculatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class CalculatorController {

    private final CalculatorService calculator;

    public CalculatorController(CalculatorService calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/teszt")
    public String calculator(Model model) {
        model.addAttribute("operation", "add");
        return "teszt";
    }

    @PostMapping("/teszt")
    public String calculate(@RequestParam(defaultValue = "") String left,
                            @RequestParam(defaultValue = "") String right,
                            @RequestParam(defaultValue = "") String operation,
                            Model model) {
        model.addAttribute("left", left);
        model.addAttribute("right", right);
        model.addAttribute("operation", operation);
        try {
            BigDecimal result = calculator.calculate(new BigDecimal(left), new BigDecimal(right), operation);
            model.addAttribute("result", result.stripTrailingZeros().toPlainString());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Mindkét mezőben érvényes számot adjon meg.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "teszt";
    }
}
