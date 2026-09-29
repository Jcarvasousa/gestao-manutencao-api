package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioCustoSetoresResponseDTO(
        Integer mes,
        Integer ano,
        List<RelatorioCustoSetorDTO> setores,
        BigDecimal totalGeral
) {
}
