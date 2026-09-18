package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record SolicitacaoCompraRequestDTO(
        Long pecaId,
        Long manutencaoId,
        Integer quantidadeNecessaria,
        String fornecedor,
        BigDecimal valorOrcamento
) {
}