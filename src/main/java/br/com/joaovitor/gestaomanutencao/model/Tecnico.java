package br.com.joaovitor.gestaomanutencao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "tecnico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tecnico {

    private static final int DIAS_UTEIS_MEDIOS_POR_MES = 22;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal salarioMensal;

    @Column(nullable = false)
    private BigDecimal cargaHorariaDiaria;

    @Column(nullable = false)
    private Boolean ativo = true;

    public BigDecimal getCustoPorHora() {
        BigDecimal horasMensais = cargaHorariaDiaria.multiply(BigDecimal.valueOf(DIAS_UTEIS_MEDIOS_POR_MES));
        return salarioMensal.divide(horasMensais, 2, RoundingMode.HALF_UP);
    }
}
