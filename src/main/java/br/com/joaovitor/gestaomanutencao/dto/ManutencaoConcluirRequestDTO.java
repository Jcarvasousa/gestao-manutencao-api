package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record ManutencaoConcluirRequestDTO(
        String descricaoServico,
        BigDecimal custoMaoDeObra
) {
}
