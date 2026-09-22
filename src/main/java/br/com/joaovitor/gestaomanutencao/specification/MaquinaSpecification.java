package br.com.joaovitor.gestaomanutencao.specification;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
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
                        criteriaBuilder.lower(root.get("setor")),
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
}
