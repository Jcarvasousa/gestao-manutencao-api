package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.OrcamentoMensal;

import java.math.BigDecimal;

public record OrcamentoMensalResponseDTO(
        Long id,
        Integer mes,
        Integer ano,
        BigDecimal valorPlanejado
) {
    public static OrcamentoMensalResponseDTO fromEntity(OrcamentoMensal entity) {
        return new OrcamentoMensalResponseDTO(
                entity.getId(),
                entity.getMes(),
                entity.getAno(),
                entity.getValorPlanejado()
        );
    }
}
