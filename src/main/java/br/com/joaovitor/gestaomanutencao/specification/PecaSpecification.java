package br.com.joaovitor.gestaomanutencao.specification;

import br.com.joaovitor.gestaomanutencao.model.Peca;
import org.springframework.data.jpa.domain.Specification;

public final class PecaSpecification {

    private PecaSpecification() {
    }

    public static Specification<Peca> comCategoria(String categoria) {
        return (root, query, criteriaBuilder) -> categoria == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("categoria")),
                        "%" + categoria.toLowerCase() + "%"
                );
    }

    public static Specification<Peca> comCodigo(String codigo) {
        return (root, query, criteriaBuilder) -> codigo == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("codigo")),
                        "%" + codigo.toLowerCase() + "%"
                );
    }
}
