package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponseDTO(
        Long id,
        Long pecaId,
        String pecaCodigo,
        Long manutencaoId,
        TipoMovimentacao tipo,
        Integer quantidade,
        BigDecimal custoUnitarioMomento,
        String observacao,
        LocalDateTime dataHora
) {
    public static MovimentacaoEstoqueResponseDTO fromEntity(MovimentacaoEstoque entity) {
        return new MovimentacaoEstoqueResponseDTO(
                entity.getId(),
                entity.getPeca().getId(),
                entity.getPeca().getCodigo(),
                entity.getManutencao() == null ? null : entity.getManutencao().getId(),
                entity.getTipo(),
                entity.getQuantidade(),
                entity.getCustoUnitarioMomento(),
                entity.getObservacao(),
                entity.getDataHora()
        );
    }
}