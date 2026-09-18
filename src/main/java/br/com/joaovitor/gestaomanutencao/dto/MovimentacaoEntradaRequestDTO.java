package br.com.joaovitor.gestaomanutencao.dto;

public record MovimentacaoEntradaRequestDTO(
        Long pecaId,
        Integer quantidade,
        String observacao
) {
}