package br.com.joaovitor.gestaomanutencao.dto;

import java.math.BigDecimal;

public record ManutencaoConcluirRequestDTO(
        Long tecnicoId,
        String descricaoServico,
        Boolean maquinaLiberadaParaUso,
        String condicoesSeguranca,
        BigDecimal horasTecnico
) {
}
