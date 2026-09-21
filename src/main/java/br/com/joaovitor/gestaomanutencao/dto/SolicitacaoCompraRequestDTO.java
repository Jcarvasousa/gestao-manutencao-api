package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SolicitacaoCompraRequestDTO(
        @NotNull
        Long pecaId,
        Long manutencaoId,
        @NotNull
        @Min(1)
        Integer quantidadeNecessaria,
        String fornecedor,
        @PositiveOrZero
        BigDecimal valorOrcamento
) {
}