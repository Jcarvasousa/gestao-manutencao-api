package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long>, JpaSpecificationExecutor<Manutencao> {

    List<Manutencao> findByMaquinaId(Long maquinaId);

    List<Manutencao> findByStatus(StatusManutencao status);

    @Override
    @EntityGraph(attributePaths = "maquina")
    Page<Manutencao> findAll(Specification<Manutencao> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "maquina")
    Optional<Manutencao> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT m
            FROM Manutencao m
            JOIN FETCH m.maquina
            WHERE m.id = :id
            """)
    Optional<Manutencao> buscarPorIdComTrava(@Param("id") Long id);

    @Query("""
            SELECT COUNT(m)
            FROM Manutencao m
            WHERE m.status NOT IN (
                br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CONCLUIDA,
                br.com.joaovitor.gestaomanutencao.model.StatusManutencao.CANCELADA
            )
            """)
    Long contarBacklog();

    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (data_conclusao - data_inicio)) / 3600)
            FROM manutencao
            WHERE tipo = 'CORRETIVA'
              AND status = 'CONCLUIDA'
              AND data_inicio IS NOT NULL
            """, nativeQuery = true)
    Double calcularMttrHoras();
}
