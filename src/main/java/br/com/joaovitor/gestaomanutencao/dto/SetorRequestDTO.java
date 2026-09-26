package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotBlank;

public record SetorRequestDTO(
        @NotBlank
        String nome
) {
}
