package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioGastoRealizadoDTO(
        Integer mes,
        Integer ano,
        BigDecimal valorGasto
) {
}
