package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.UnidadeMedida;

import java.math.BigDecimal;

public record PecaUsadaResponseDTO(
        Long pecaId,
        String pecaCodigo,
        String pecaNome,
        UnidadeMedida unidadeMedida,
        Integer quantidadeUsada,
        BigDecimal custoTotal,
        Integer saldoDevolvivel
) {
}
