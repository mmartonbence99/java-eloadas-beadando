package org.example.ea_beadando.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

@Service
public class CalculatorService {

    public BigDecimal calculate(BigDecimal left, BigDecimal right, String operation) {
        return switch (operation) {
            case "add" -> left.add(right);
            case "subtract" -> left.subtract(right);
            case "multiply" -> left.multiply(right);
            case "divide" -> {
                if (right.signum() == 0) {
                    throw new IllegalArgumentException("Nullával nem lehet osztani.");
                }
                yield left.divide(right, MathContext.DECIMAL128);
            }
            default -> throw new IllegalArgumentException("Érvénytelen művelet.");
        };
    }
}
