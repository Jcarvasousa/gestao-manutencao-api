package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioCustoMensalDTO(
        Integer mes,
        Integer ano,
        BigDecimal custoPecas,
        BigDecimal custoMaoDeObra,
        BigDecimal custoTotal
) {
}
