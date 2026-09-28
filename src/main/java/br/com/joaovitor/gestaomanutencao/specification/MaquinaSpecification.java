package br.com.joaovitor.gestaomanutencao.specification;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class MaquinaSpecification {

    private MaquinaSpecification() {
    }

    public static Specification<Maquina> comStatus(StatusMaquina status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Maquina> comSetor(String setor) {
        return (root, query, criteriaBuilder) -> setor == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("setor").get("nome")),
                        "%" + setor.toLowerCase() + "%"
                );
    }

    public static Specification<Maquina> comCodigo(String codigo) {
        return (root, query, criteriaBuilder) -> codigo == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("codigo")),
                        "%" + codigo.toLowerCase() + "%"
                );
    }

    public static Specification<Maquina> comBusca(String busca) {
        return (root, query, criteriaBuilder) -> {
            if (busca == null || busca.isBlank()) {
                return null;
            }
            String padrao = "%" + busca.trim().toLowerCase() + "%";
            Join<Maquina, Setor> setor = root.join("setor", JoinType.LEFT);
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("codigo")), padrao),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), padrao),
                    criteriaBuilder.like(criteriaBuilder.lower(setor.get("nome")), padrao)
            );
        };
    }
}
