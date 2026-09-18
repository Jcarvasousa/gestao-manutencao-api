package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.Peca;

import java.math.BigDecimal;

public record PecaResponseDTO(
        Long id,
        String codigo,
        String nome,
        String categoria,
        String localizacaoFisica,
        Integer quantidadeAtual,
        Integer estoqueMinimo,
        BigDecimal custoUnitario
) {
    public static PecaResponseDTO fromEntity(Peca entity) {
        return new PecaResponseDTO(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getCategoria(),
                entity.getLocalizacaoFisica(),
                entity.getQuantidadeAtual(),
                entity.getEstoqueMinimo(),
                entity.getCustoUnitario()
        );
    }
}