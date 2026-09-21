package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.NotBlank;

public record MaquinaRequestDTO(
        @NotBlank
        String codigo,
        @NotBlank
        String descricao,
        String setor
) {
}