package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>,
        JpaSpecificationExecutor<MovimentacaoEstoque> {

    List<MovimentacaoEstoque> findByPecaId(Long pecaId);

    List<MovimentacaoEstoque> findByManutencaoId(Long manutencaoId);

    @Query("""
            SELECT COALESCE(SUM(m.custoUnitarioMomento * m.quantidade), 0)
            FROM MovimentacaoEstoque m
            WHERE m.tipo = br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao.SAIDA
              AND MONTH(m.dataHora) = :mes
              AND YEAR(m.dataHora) = :ano
            """)
    BigDecimal calcularCustoPecasPorMesEAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    @Query("""
            SELECT COALESCE(SUM(m.custoUnitarioMomento * m.quantidade), 0)
            FROM MovimentacaoEstoque m
            WHERE m.tipo = br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao.SAIDA
              AND m.manutencao.maquina.id = :maquinaId
              AND MONTH(m.dataHora) = :mes
              AND YEAR(m.dataHora) = :ano
            """)
    BigDecimal calcularCustoPecasPorMaquinaMesEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    @Query("""
            SELECT COALESCE(SUM(m.custoUnitarioMomento * m.quantidade), 0)
            FROM MovimentacaoEstoque m
            WHERE m.tipo = br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao.SAIDA
              AND m.manutencao.maquina.id = :maquinaId
              AND YEAR(m.dataHora) = :ano
            """)
    BigDecimal calcularCustoPecasPorMaquinaEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("ano") Integer ano
    );

    @Query("""
            SELECT COALESCE(SUM(m.custoUnitarioMomento * m.quantidade), 0)
            FROM MovimentacaoEstoque m
            WHERE m.tipo = br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao.SAIDA
              AND m.manutencao.maquina.id = :maquinaId
            """)
    BigDecimal calcularCustoPecasPorMaquinaTotal(@Param("maquinaId") Long maquinaId);
}
