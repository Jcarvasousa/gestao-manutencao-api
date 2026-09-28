package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

// Modo 1: valorApurado direto. Modo 2: horasTrabalhadas + valorHora (o backend calcula o valorApurado).
public record ServicoTerceiroRequestDTO(
        @NotNull
        Long manutencaoId,
        @PositiveOrZero
        BigDecimal valorApurado,
        @Positive
        BigDecimal horasTrabalhadas,
        @PositiveOrZero
        BigDecimal valorHora,
        @NotBlank
        String descricao,
        String fornecedor
) {
}
