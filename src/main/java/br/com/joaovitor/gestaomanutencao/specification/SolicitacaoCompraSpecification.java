package br.com.joaovitor.gestaomanutencao.specification;

import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra;
import org.springframework.data.jpa.domain.Specification;

public final class SolicitacaoCompraSpecification {

    private SolicitacaoCompraSpecification() {
    }

    public static Specification<SolicitacaoCompra> comStatus(StatusSolicitacaoCompra status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<SolicitacaoCompra> comPecaId(Long pecaId) {
        return (root, query, criteriaBuilder) -> pecaId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("peca").get("id"), pecaId);
    }
}
