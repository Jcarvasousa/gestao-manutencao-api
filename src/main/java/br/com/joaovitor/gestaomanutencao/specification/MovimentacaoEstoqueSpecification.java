package br.com.joaovitor.gestaomanutencao.specification;

import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;
import org.springframework.data.jpa.domain.Specification;

public final class MovimentacaoEstoqueSpecification {

    private MovimentacaoEstoqueSpecification() {
    }

    public static Specification<MovimentacaoEstoque> comTipo(TipoMovimentacao tipo) {
        return (root, query, criteriaBuilder) -> tipo == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("tipo"), tipo);
    }

    public static Specification<MovimentacaoEstoque> comPecaId(Long pecaId) {
        return (root, query, criteriaBuilder) -> pecaId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("peca").get("id"), pecaId);
    }

    public static Specification<MovimentacaoEstoque> comManutencaoId(Long manutencaoId) {
        return (root, query, criteriaBuilder) -> manutencaoId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("manutencao").get("id"), manutencaoId);
    }
}
