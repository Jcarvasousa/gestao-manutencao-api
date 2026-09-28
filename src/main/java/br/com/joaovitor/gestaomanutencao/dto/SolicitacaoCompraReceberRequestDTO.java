package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SolicitacaoCompraReceberRequestDTO(
        @NotNull
        @Positive
        BigDecimal valorOrcamento
) {
}
