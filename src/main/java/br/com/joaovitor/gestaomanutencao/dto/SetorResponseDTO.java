package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Setor;

public record SetorResponseDTO(
        Long id,
        String nome
) {
    public static SetorResponseDTO fromEntity(Setor entity) {
        return new SetorResponseDTO(
                entity.getId(),
                entity.getNome()
        );
    }
}
