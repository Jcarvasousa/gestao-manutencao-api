package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioOrcamentoMensalDTO(
        Integer mes,
        Integer ano,
        BigDecimal valorPlanejado,
        BigDecimal valorRealizado,
        BigDecimal saldoDisponivel,
        BigDecimal percentualUtilizado
) {
}
