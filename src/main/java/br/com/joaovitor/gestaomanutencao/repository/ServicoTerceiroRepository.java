package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

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
            SELECT m FROM Manutencao m
            JOIN FETCH m.tecnico
            WHERE m.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND MONTH(m.dataConclusao) = :mes
              AND YEAR(m.dataConclusao) = :ano
            """)
    List<Manutencao> buscarManutencoesConcluidasComTecnicoPorMesEAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    default BigDecimal calcularCustoMaoDeObraPorMesEAno(Integer mes, Integer ano) {
        BigDecimal custoServicoTerceiro = somarValorServicoTerceiroPorMesEAno(mes, ano);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                buscarManutencoesConcluidasComTecnicoPorMesEAno(mes, ano)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

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
            SELECT m FROM Manutencao m
            JOIN FETCH m.tecnico
            WHERE m.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND m.maquina.id = :maquinaId
              AND MONTH(m.dataConclusao) = :mes
              AND YEAR(m.dataConclusao) = :ano
            """)
    List<Manutencao> buscarManutencoesConcluidasComTecnicoPorMaquinaMesEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    default BigDecimal calcularCustoMaoDeObraPorMaquinaMesEAno(Long maquinaId, Integer mes, Integer ano) {
        BigDecimal custoServicoTerceiro = somarValorServicoTerceiroPorMaquinaMesEAno(maquinaId, mes, ano);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                buscarManutencoesConcluidasComTecnicoPorMaquinaMesEAno(maquinaId, mes, ano)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

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
            SELECT m FROM Manutencao m
            JOIN FETCH m.tecnico
            WHERE m.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND m.maquina.id = :maquinaId
              AND YEAR(m.dataConclusao) = :ano
            """)
    List<Manutencao> buscarManutencoesConcluidasComTecnicoPorMaquinaEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("ano") Integer ano
    );

    default BigDecimal calcularCustoMaoDeObraPorMaquinaEAno(Long maquinaId, Integer ano) {
        BigDecimal custoServicoTerceiro = somarValorServicoTerceiroPorMaquinaEAno(maquinaId, ano);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                buscarManutencoesConcluidasComTecnicoPorMaquinaEAno(maquinaId, ano)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

    @Query("""
            SELECT COALESCE(SUM(st.valor), 0)
            FROM ServicoTerceiro st
            WHERE st.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND st.manutencao.maquina.id = :maquinaId
            """)
    BigDecimal somarValorServicoTerceiroPorMaquinaTotal(@Param("maquinaId") Long maquinaId);

    @Query("""
            SELECT m FROM Manutencao m
            JOIN FETCH m.tecnico
            WHERE m.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND m.maquina.id = :maquinaId
            """)
    List<Manutencao> buscarManutencoesConcluidasComTecnicoPorMaquinaTotal(@Param("maquinaId") Long maquinaId);

    default BigDecimal calcularCustoMaoDeObraPorMaquinaTotal(Long maquinaId) {
        BigDecimal custoServicoTerceiro = somarValorServicoTerceiroPorMaquinaTotal(maquinaId);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                buscarManutencoesConcluidasComTecnicoPorMaquinaTotal(maquinaId)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

    private BigDecimal somarCustoMaoDeObraInterna(List<Manutencao> manutencoes) {
        return manutencoes.stream()
                .map(manutencao -> manutencao.getHorasTecnico().multiply(manutencao.getTecnico().getCustoPorHora()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
