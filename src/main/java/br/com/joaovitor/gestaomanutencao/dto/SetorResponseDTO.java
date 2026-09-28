package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Setor;

public record SetorResponseDTO(
        Long id,
        String nome,
        Boolean ativo
) {
    public static SetorResponseDTO fromEntity(Setor entity) {
        return new SetorResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getAtivo()
        );
    }
}
