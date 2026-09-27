package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManutencaoTecnicoRepository extends JpaRepository<ManutencaoTecnico, Long> {

    boolean existsByManutencaoId(Long manutencaoId);

    @EntityGraph(attributePaths = "tecnico")
    List<ManutencaoTecnico> findByManutencaoId(Long manutencaoId);

    @EntityGraph(attributePaths = "tecnico")
    @Query("""
            SELECT mt FROM ManutencaoTecnico mt
            WHERE mt.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND MONTH(mt.manutencao.dataConclusao) = :mes
              AND YEAR(mt.manutencao.dataConclusao) = :ano
            """)
    List<ManutencaoTecnico> buscarManutencaoTecnicosConcluidosPorMesEAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    @EntityGraph(attributePaths = "tecnico")
    @Query("""
            SELECT mt FROM ManutencaoTecnico mt
            WHERE mt.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND mt.manutencao.maquina.id = :maquinaId
              AND MONTH(mt.manutencao.dataConclusao) = :mes
              AND YEAR(mt.manutencao.dataConclusao) = :ano
            """)
    List<ManutencaoTecnico> buscarManutencaoTecnicosConcluidosPorMaquinaMesEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano
    );

    @EntityGraph(attributePaths = "tecnico")
    @Query("""
            SELECT mt FROM ManutencaoTecnico mt
            WHERE mt.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND mt.manutencao.maquina.id = :maquinaId
              AND YEAR(mt.manutencao.dataConclusao) = :ano
            """)
    List<ManutencaoTecnico> buscarManutencaoTecnicosConcluidosPorMaquinaEAno(
            @Param("maquinaId") Long maquinaId,
            @Param("ano") Integer ano
    );

    @EntityGraph(attributePaths = "tecnico")
    @Query("""
            SELECT mt FROM ManutencaoTecnico mt
            WHERE mt.manutencao.status = br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA
              AND mt.manutencao.maquina.id = :maquinaId
            """)
    List<ManutencaoTecnico> buscarManutencaoTecnicosConcluidosPorMaquinaTotal(@Param("maquinaId") Long maquinaId);
}
