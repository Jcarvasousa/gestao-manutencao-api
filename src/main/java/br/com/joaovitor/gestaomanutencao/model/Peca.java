package br.com.joaovitor.gestaomanutencao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "peca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    private String categoria;

    private String unidadeMedida;

    private String localizacaoFisica;

    @Min(0)
    @Column(nullable = false)
    private Integer quantidadeAtual = 0;

    private Integer estoqueMinimo = 0;

    private BigDecimal custoUnitario;
}
