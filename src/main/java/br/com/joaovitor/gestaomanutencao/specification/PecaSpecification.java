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

    public static Specification<Peca> comBusca(String busca) {
        return (root, query, criteriaBuilder) -> {
            if (busca == null || busca.isBlank()) {
                return null;
            }
            String padrao = "%" + busca.trim().toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("codigo")), padrao),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), padrao),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("categoria")), padrao)
            );
        };
    }
}
