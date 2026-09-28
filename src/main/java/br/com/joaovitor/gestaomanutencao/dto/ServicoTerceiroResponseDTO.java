package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;

import java.math.BigDecimal;

public record ServicoTerceiroResponseDTO(
        Long id,
        Long manutencaoId,
        BigDecimal valorApurado,
        BigDecimal horasTrabalhadas,
        BigDecimal valorHora,
        BigDecimal valorFinal,
        String descricao,
        String fornecedor,
        String observacao
) {
    public static ServicoTerceiroResponseDTO fromEntity(ServicoTerceiro entity) {
        return new ServicoTerceiroResponseDTO(
                entity.getId(),
                entity.getManutencao().getId(),
                entity.getValorApurado(),
                entity.getHorasTrabalhadas(),
                entity.getValorHora(),
                entity.getValorFinal(),
                entity.getDescricao(),
                entity.getFornecedor(),
                entity.getObservacao()
        );
    }
}
