package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long>,
        JpaSpecificationExecutor<SolicitacaoCompra> {

    Optional<SolicitacaoCompra> findFirstByPecaIdAndStatusIn(
            Long pecaId,
            List<StatusSolicitacaoCompra> statusList
    );

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

    @Query("""
            SELECT COALESCE(SUM(s.valorOrcamento), 0)
            FROM SolicitacaoCompra s
            WHERE s.status = br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra.RECEBIDA
              AND YEAR(s.dataRecebimento) = :ano
            """)
    BigDecimal calcularGastoRealizadoPorAno(@Param("ano") Integer ano);
}
