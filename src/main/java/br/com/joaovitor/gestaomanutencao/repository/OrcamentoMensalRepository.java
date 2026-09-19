package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.OrcamentoMensal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrcamentoMensalRepository extends JpaRepository<OrcamentoMensal, Long> {

    Optional<OrcamentoMensal> findByMesAndAno(Integer mes, Integer ano);
}
