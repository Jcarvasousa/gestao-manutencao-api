package br.com.joaovitor.gestaomanutencao.dto;

public record ManutencaoRequestDTO(
        Long maquinaId,
        String problemaDescricao,
        String tecnicoResponsavel
) {
}