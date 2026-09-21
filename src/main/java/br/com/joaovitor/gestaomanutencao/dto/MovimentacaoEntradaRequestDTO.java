package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimentacaoEntradaRequestDTO(
        @NotNull
        Long pecaId,
        @NotNull
        @Min(1)
        Integer quantidade,
        String observacao
) {
}