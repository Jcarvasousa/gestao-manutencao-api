package br.com.joaovitor.gestaomanutencao.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PercentualCalculator {

    private static final int ESCALA_INTERMEDIARIA = 10;

    private PercentualCalculator() {
    }

    public static BigDecimal calcular(BigDecimal realizado, BigDecimal planejado) {
        if (planejado == null || planejado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal valorRealizado = realizado == null ? BigDecimal.ZERO : realizado;
        return valorRealizado
                .multiply(BigDecimal.valueOf(100))
                .divide(planejado, ESCALA_INTERMEDIARIA, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
