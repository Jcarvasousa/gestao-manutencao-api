package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record RelatorioKpisDTO(
        Long backlogQuantidade,
        BigDecimal mttrHoras
) {
}
