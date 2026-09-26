package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;

import java.time.LocalDateTime;

public record MaquinaResponseDTO(
        Long id,
        String codigo,
        String descricao,
        Long setorId,
        String setorNome,
        StatusMaquina status,
        LocalDateTime criadaEm,
        LocalDateTime atualizadaEm
) {
    public static MaquinaResponseDTO fromEntity(Maquina entity) {
        return new MaquinaResponseDTO(
                entity.getId(),
                entity.getCodigo(),
                entity.getDescricao(),
                entity.getSetor() == null ? null : entity.getSetor().getId(),
                entity.getSetor() == null ? null : entity.getSetor().getNome(),
                entity.getStatus(),
                entity.getCriadaEm(),
                entity.getAtualizadaEm()
        );
    }
}
