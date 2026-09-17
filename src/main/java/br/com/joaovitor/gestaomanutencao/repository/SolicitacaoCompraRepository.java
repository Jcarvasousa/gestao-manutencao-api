package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long> {
}
