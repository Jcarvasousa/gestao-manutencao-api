package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioCustoSetorDTO(
        Long setorId,
        String setorNome,
        Boolean ativo,
        Integer quantidadeMaquinas,
        BigDecimal custoPecas,
        BigDecimal custoMaoDeObra,
        BigDecimal custoTotal,
        BigDecimal percentualDoTotal
) {
}
