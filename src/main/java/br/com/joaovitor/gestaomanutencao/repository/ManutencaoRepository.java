package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    List<Manutencao> findByMaquinaId(Long maquinaId);

    List<Manutencao> findByStatus(StatusManutencao status);

    @Query("""
            SELECT COALESCE(SUM(m.custoMaoDeObra), 0)
            FROM Manutencao m
            WHERE m.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND MONTH(m.dataConclusao) = :mes
              AND YEAR(m.dataConclusao) = :ano
            """)
    BigDecimal calcularCustoMaoDeObraPorMesEAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );
}
