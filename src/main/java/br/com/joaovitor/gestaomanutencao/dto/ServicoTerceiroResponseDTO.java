package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;

import java.math.BigDecimal;

public record ServicoTerceiroResponseDTO(
        Long id,
        Long manutencaoId,
        BigDecimal valor,
        String descricao,
        String fornecedor
) {
    public static ServicoTerceiroResponseDTO fromEntity(ServicoTerceiro entity) {
        return new ServicoTerceiroResponseDTO(
                entity.getId(),
                entity.getManutencao().getId(),
                entity.getValor(),
                entity.getDescricao(),
                entity.getFornecedor()
        );
    }
}
