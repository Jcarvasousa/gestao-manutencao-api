package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ServicoTerceiroRequestDTO(
        @NotNull
        Long manutencaoId,
        @NotNull
        @PositiveOrZero
        BigDecimal valor,
        @NotBlank
        String descricao,
        String fornecedor
) {
}
