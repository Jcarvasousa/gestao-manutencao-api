package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;

import java.math.BigDecimal;

public record ManutencaoTecnicoResponseDTO(
        Long id,
        Long manutencaoId,
        Long tecnicoId,
        String tecnicoNome,
        BigDecimal horasTrabalhadas,
        BigDecimal custoPorHora,
        BigDecimal custoTotal
) {
    public static ManutencaoTecnicoResponseDTO fromEntity(ManutencaoTecnico entity) {
        BigDecimal custoPorHora = entity.getTecnico().getCustoPorHora();
        return new ManutencaoTecnicoResponseDTO(
                entity.getId(),
                entity.getManutencao().getId(),
                entity.getTecnico().getId(),
                entity.getTecnico().getNome(),
                entity.getHorasTrabalhadas(),
                custoPorHora,
                entity.getHorasTrabalhadas().multiply(custoPorHora)
        );
    }
}
