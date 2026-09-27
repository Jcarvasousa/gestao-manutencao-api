package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ManutencaoTecnicoRequestDTO(
        @NotNull
        Long tecnicoId,
        @NotNull
        @Positive
        BigDecimal horasTrabalhadas
) {
}
