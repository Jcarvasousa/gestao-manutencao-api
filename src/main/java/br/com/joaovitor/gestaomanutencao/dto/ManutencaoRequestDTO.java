package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ManutencaoRequestDTO(
        @NotNull
        Long maquinaId,
        @NotBlank
        String problemaDescricao,
        @NotNull
        TipoManutencao tipo,
        String tecnicoResponsavel
) {
}