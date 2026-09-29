package br.com.joaovitor.gestaomanutencao.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentualCalculatorTest {

    @Test
    void calculaPercentualComPrecisaoSemPerderCasasDecimais() {
        BigDecimal resultado = PercentualCalculator.calcular(
                new BigDecimal("1759.25"), new BigDecimal("73013.00"));

        assertEquals(new BigDecimal("2.41"), resultado);
    }

    @Test
    void calculaPercentualSimples() {
        BigDecimal resultado = PercentualCalculator.calcular(
                new BigDecimal("100"), new BigDecimal("200"));

        assertEquals(new BigDecimal("50.00"), resultado);
    }

    @Test
    void realizadoZeroResultaEmPercentualZero() {
        BigDecimal resultado = PercentualCalculator.calcular(
                BigDecimal.ZERO, new BigDecimal("1000"));

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void permitePercentualAcimaDeCemPorCento() {
        BigDecimal resultado = PercentualCalculator.calcular(
                new BigDecimal("1500"), new BigDecimal("1000"));

        assertEquals(new BigDecimal("150.00"), resultado);
    }

    @Test
    void planejadoZeroMantemComportamentoAtualRetornandoZero() {
        BigDecimal resultado = PercentualCalculator.calcular(
                new BigDecimal("500"), BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    void planejadoNuloMantemComportamentoAtualRetornandoZero() {
        BigDecimal resultado = PercentualCalculator.calcular(
                new BigDecimal("500"), null);

        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    void arredondaHalfUpUmTercoParaCima() {
        BigDecimal resultado = PercentualCalculator.calcular(
                BigDecimal.ONE, new BigDecimal("3"));

        assertEquals(new BigDecimal("33.33"), resultado);
    }

    @Test
    void arredondaHalfUpDoisTercosParaCima() {
        BigDecimal resultado = PercentualCalculator.calcular(
                new BigDecimal("2"), new BigDecimal("3"));

        assertEquals(new BigDecimal("66.67"), resultado);
    }
}
