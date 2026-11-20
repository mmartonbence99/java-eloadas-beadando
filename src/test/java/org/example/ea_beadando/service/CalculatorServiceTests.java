package org.example.ea_beadando.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTests {

    private final CalculatorService calculator = new CalculatorService();

    @Test
    void calculatesFourOperations() {
        assertEquals(new BigDecimal("7.5"), calculator.calculate(new BigDecimal("5.5"), new BigDecimal("2"), "add"));
        assertEquals(new BigDecimal("3.5"), calculator.calculate(new BigDecimal("5.5"), new BigDecimal("2"), "subtract"));
        assertEquals(new BigDecimal("11.0"), calculator.calculate(new BigDecimal("5.5"), new BigDecimal("2"), "multiply"));
        assertEquals(new BigDecimal("2.75"), calculator.calculate(new BigDecimal("5.5"), new BigDecimal("2"), "divide"));
    }

    @Test
    void dividesRepeatingDecimalsWithDecimal128Precision() {
        assertEquals(new BigDecimal("0.3333333333333333333333333333333333"),
                calculator.calculate(BigDecimal.ONE, new BigDecimal("3"), "divide"));
    }

    @Test
    void rejectsDivisionByZero() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(BigDecimal.ONE, BigDecimal.ZERO, "divide"));
        assertEquals("Nullával nem lehet osztani.", error.getMessage());
    }

    @Test
    void rejectsUnknownOperation() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(BigDecimal.ONE, BigDecimal.ONE, "unknown"));
        assertEquals("Érvénytelen művelet.", error.getMessage());
    }
}
