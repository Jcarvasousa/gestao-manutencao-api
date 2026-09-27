package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
