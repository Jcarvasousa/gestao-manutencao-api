package br.com.joaovitor.gestaomanutencao.dto;

public record MovimentacaoSaidaRequestDTO(
        Long pecaId,
        Long manutencaoId,
        Integer quantidade,
        String observacao
) {
}