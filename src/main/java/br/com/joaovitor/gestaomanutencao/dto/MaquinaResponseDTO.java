package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;

import java.time.LocalDateTime;

public record MaquinaResponseDTO(
        Long id,
        String codigo,
        String descricao,
        String setor,
        StatusMaquina status,
        LocalDateTime criadaEm,
        LocalDateTime atualizadaEm
) {
    public static MaquinaResponseDTO fromEntity(Maquina entity) {
        return new MaquinaResponseDTO(
                entity.getId(),
                entity.getCodigo(),
                entity.getDescricao(),
                entity.getSetor(),
                entity.getStatus(),
                entity.getCriadaEm(),
                entity.getAtualizadaEm()
        );
    }
}