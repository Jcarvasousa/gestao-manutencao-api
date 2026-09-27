package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioCustoMaquinasResponseDTO(
        List<RelatorioCustoMaquinaDTO> maquinas,
        BigDecimal totalGeral
) {
}
