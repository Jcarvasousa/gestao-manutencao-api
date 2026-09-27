package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TecnicoRequestDTO(
        @NotBlank
        String nome,
        @NotNull
        @PositiveOrZero
        BigDecimal salarioMensal,
        @NotNull
        @Positive
        BigDecimal cargaHorariaDiaria,
        Boolean ativo
) {
}
