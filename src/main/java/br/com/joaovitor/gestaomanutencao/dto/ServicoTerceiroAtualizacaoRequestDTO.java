package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ServicoTerceiroAtualizacaoRequestDTO(
        @PositiveOrZero
        BigDecimal valorFinal,
        String observacao
) {
}
