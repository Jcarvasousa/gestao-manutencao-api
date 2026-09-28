package br.com.joaovitor.gestaomanutencao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "servico_terceiro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicoTerceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manutencao_id", nullable = false)
    private Manutencao manutencao;

    @Column(name = "valor", nullable = false)
    private BigDecimal valorApurado;

    private BigDecimal horasTrabalhadas;

    private BigDecimal valorHora;

    private BigDecimal valorFinal;

    @Column(nullable = false)
    private String descricao;

    private String fornecedor;

    private String observacao;
}
