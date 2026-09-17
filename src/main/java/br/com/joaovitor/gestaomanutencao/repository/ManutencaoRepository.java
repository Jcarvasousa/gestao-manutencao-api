package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    List<Manutencao> findByMaquinaId(Long maquinaId);

    List<Manutencao> findByStatus(StatusManutencao status);
}
