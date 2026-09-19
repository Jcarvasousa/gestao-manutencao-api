package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;

public record ManutencaoRequestDTO(
        Long maquinaId,
        String problemaDescricao,
        TipoManutencao tipo,
        String tecnicoResponsavel
) {
}