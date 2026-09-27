package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Tecnico;

import java.math.BigDecimal;

public record TecnicoResponseDTO(
        Long id,
        String nome,
        BigDecimal salarioMensal,
        BigDecimal cargaHorariaDiaria,
        Boolean ativo,
        BigDecimal custoPorHora
) {
    public static TecnicoResponseDTO fromEntity(Tecnico entity) {
        return new TecnicoResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getSalarioMensal(),
                entity.getCargaHorariaDiaria(),
                entity.getAtivo(),
                entity.getCustoPorHora()
        );
    }
}
