package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
}
