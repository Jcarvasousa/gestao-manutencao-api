package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioOrcamentoAnualDTO(
        Integer ano,
        BigDecimal valorPlanejadoTotal,
        BigDecimal valorRealizadoTotal,
        BigDecimal saldoDisponivel,
        BigDecimal percentualUtilizado
) {
}
