package br.com.joaovitor.gestaomanutencao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peca_id", nullable = false)
    private Peca peca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manutencao_id", nullable = true)
    private Manutencao manutencao;

    @Min(1)
    @Column(nullable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoCompra status = StatusSolicitacaoCompra.AGUARDANDO_ORCAMENTO;

    private String fornecedor;

    private BigDecimal valorOrcamento;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataPrevisaoEntrega;

    private LocalDateTime dataRecebimento;

    @PrePersist
    public void prePersist() {
        this.dataSolicitacao = LocalDateTime.now();
    }
}
