package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ServicoTerceiroRepository extends JpaRepository<ServicoTerceiro, Long> {

    boolean existsByManutencaoId(Long manutencaoId);

    @EntityGraph(attributePaths = "manutencao")
    Page<ServicoTerceiro> findByManutencaoId(Long manutencaoId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "manutencao")
    Page<ServicoTerceiro> findAll(Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(st.valor), 0)
            FROM ServicoTerceiro st
            WHERE st.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND MONTH(st.manutencao.dataConclusao) = :mes
              AND YEAR(st.manutencao.dataConclusao) = :ano
            """)
    BigDecimal somarValorServicoTerceiroPorMesEAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    @Query("""
            SELECT COALESCE(SUM(st.valor), 0)
            FROM ServicoTerceiro st
            WHERE st.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND st.manutencao.maquina.id = :maquinaId
              AND MONTH(st.manutencao.dataConclusao) = :mes
              AND YEAR(st.manutencao.dataConclusao) = :ano
            """)
    BigDecimal somarValorServicoTerceiroPorMaquinaMesEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    @Query("""
            SELECT COALESCE(SUM(st.valor), 0)
            FROM ServicoTerceiro st
            WHERE st.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND st.manutencao.maquina.id = :maquinaId
              AND YEAR(st.manutencao.dataConclusao) = :ano
            """)
    BigDecimal somarValorServicoTerceiroPorMaquinaEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("ano") Integer ano
    );

    @Query("""
            SELECT COALESCE(SUM(st.valor), 0)
            FROM ServicoTerceiro st
            WHERE st.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND st.manutencao.maquina.id = :maquinaId
            """)
    BigDecimal somarValorServicoTerceiroPorMaquinaTotal(@Param("maquinaId") Long maquinaId);
}
