package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record PecaRequestDTO(
        String codigo,
        String nome,
        String categoria,
        String localizacaoFisica,
        Integer quantidadeAtual,
        Integer estoqueMinimo,
        BigDecimal custoUnitario
) {
}