package br.com.joaovitor.gestaomanutencao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PecaRequestDTO(
        @NotBlank
        String codigo,
        @NotBlank
        String nome,
        String categoria,
        String unidadeMedida,
        String localizacaoFisica,
        @NotNull
        @Min(0)
        Integer quantidadeAtual,
        @Min(0)
        Integer estoqueMinimo,
        @PositiveOrZero
        BigDecimal custoUnitario
) {
}