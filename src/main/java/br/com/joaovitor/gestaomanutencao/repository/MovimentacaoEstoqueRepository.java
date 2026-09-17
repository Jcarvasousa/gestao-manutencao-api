package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByPecaId(Long pecaId);

    List<MovimentacaoEstoque> findByManutencaoId(Long manutencaoId);
}
