package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;

import java.time.LocalDateTime;

public record ManutencaoResponseDTO(
        Long id,
        Long maquinaId,
        String maquinaCodigo,
        String problemaDescricao,
        StatusManutencao status,
        String tecnicoResponsavel,
        LocalDateTime dataAbertura,
        LocalDateTime dataInicio,
        LocalDateTime dataConclusao
) {
    public static ManutencaoResponseDTO fromEntity(Manutencao entity) {
        return new ManutencaoResponseDTO(
                entity.getId(),
                entity.getMaquina().getId(),
                entity.getMaquina().getCodigo(),
                entity.getProblemaDescricao(),
                entity.getStatus(),
                entity.getTecnicoResponsavel(),
                entity.getDataAbertura(),
                entity.getDataInicio(),
                entity.getDataConclusao()
        );
    }
}