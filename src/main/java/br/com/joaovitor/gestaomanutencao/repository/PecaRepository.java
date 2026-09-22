package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PecaRepository extends JpaRepository<Peca, Long>, JpaSpecificationExecutor<Peca> {

    Optional<Peca> findByCodigo(String codigo);
}
