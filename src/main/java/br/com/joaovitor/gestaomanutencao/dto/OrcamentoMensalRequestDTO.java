package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrcamentoMensalRequestDTO(
        @NotNull
        @Min(1)
        @Max(12)
        Integer mes,
        @NotNull
        @Min(2020)
        Integer ano,
        @NotNull
        @Positive
        BigDecimal valorPlanejado
) {
}
