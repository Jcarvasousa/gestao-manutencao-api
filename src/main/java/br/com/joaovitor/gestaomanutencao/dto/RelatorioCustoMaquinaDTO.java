package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioCustoMaquinaDTO(
        Long maquinaId,
        String maquinaCodigo,
        Integer mes,
        Integer ano,
        BigDecimal custoPecas,
        BigDecimal custoMaoDeObra,
        BigDecimal custoTotal
) {
}
