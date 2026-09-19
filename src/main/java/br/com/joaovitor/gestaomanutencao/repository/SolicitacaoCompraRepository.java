package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long> {

    @Query("""
            SELECT COALESCE(SUM(s.valorOrcamento), 0)
            FROM SolicitacaoCompra s
            WHERE s.status = br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra.RECEBIDA
              AND MONTH(s.dataRecebimento) = :mes
              AND YEAR(s.dataRecebimento) = :ano
            """)
    BigDecimal calcularGastoRealizadoPorMesEAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );
}
