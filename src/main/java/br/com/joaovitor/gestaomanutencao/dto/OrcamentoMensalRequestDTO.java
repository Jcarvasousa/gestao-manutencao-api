package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record OrcamentoMensalRequestDTO(
        Integer mes,
        Integer ano,
        BigDecimal valorPlanejado
) {
}
