package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitacaoCompraResponseDTO(
        Long id,
        Long pecaId,
        String pecaCodigo,
        String pecaNome,
        Long manutencaoId,
        Integer quantidadeNecessaria,
        StatusSolicitacaoCompra status,
        String fornecedor,
        BigDecimal valorOrcamento,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataPrevisaoEntrega,
        LocalDateTime dataRecebimento
) {
    public static SolicitacaoCompraResponseDTO fromEntity(SolicitacaoCompra entity) {
        return new SolicitacaoCompraResponseDTO(
                entity.getId(),
                entity.getPeca().getId(),
                entity.getPeca().getCodigo(),
                entity.getPeca().getNome(),
                entity.getManutencao() == null ? null : entity.getManutencao().getId(),
                entity.getQuantidadeNecessaria(),
                entity.getStatus(),
                entity.getFornecedor(),
                entity.getValorOrcamento(),
                entity.getDataSolicitacao(),
                entity.getDataPrevisaoEntrega(),
                entity.getDataRecebimento()
        );
    }
}