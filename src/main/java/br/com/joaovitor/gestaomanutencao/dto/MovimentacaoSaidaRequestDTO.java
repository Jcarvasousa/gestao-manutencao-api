package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimentacaoSaidaRequestDTO(
        @NotNull
        Long pecaId,
        @NotNull
        Long manutencaoId,
        @NotNull
        @Min(1)
        Integer quantidade,
        String observacao
) {
}