package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MovimentacaoAjusteRequestDTO(
        @NotNull
        Long pecaId,
        @NotNull
        @Min(0)
        Integer quantidadeNova,
        @NotBlank
        String observacao
) {
}
