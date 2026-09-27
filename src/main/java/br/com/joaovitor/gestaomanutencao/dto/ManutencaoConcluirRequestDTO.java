package br.com.joaovitor.gestaomanutencao.dto;

public record ManutencaoConcluirRequestDTO(
        String descricaoServico,
        Boolean maquinaLiberadaParaUso,
        String condicoesSeguranca
) {
}
