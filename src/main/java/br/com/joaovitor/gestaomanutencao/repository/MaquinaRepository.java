package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MaquinaRepository extends JpaRepository<Maquina, Long>, JpaSpecificationExecutor<Maquina> {

    @Override
    @EntityGraph(attributePaths = "setor")
    Page<Maquina> findAll(Specification<Maquina> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "setor")
    Optional<Maquina> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "setor")
    List<Maquina> findAll();

    @EntityGraph(attributePaths = "setor")
    List<Maquina> findBySetorIdIn(List<Long> setorIds);
}
