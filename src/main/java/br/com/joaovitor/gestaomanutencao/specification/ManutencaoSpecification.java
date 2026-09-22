package br.com.joaovitor.gestaomanutencao.specification;

import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ManutencaoSpecification {

    private ManutencaoSpecification() {
    }

    public static Specification<Manutencao> comStatus(StatusManutencao status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Manutencao> comTipo(TipoManutencao tipo) {
        return (root, query, criteriaBuilder) -> tipo == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("tipo"), tipo);
    }

    public static Specification<Manutencao> comMaquinaId(Long maquinaId) {
        return (root, query, criteriaBuilder) -> maquinaId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("maquina").get("id"), maquinaId);
    }

    public static Specification<Manutencao> comDataAberturaDesde(LocalDateTime dataInicio) {
        return (root, query, criteriaBuilder) -> dataInicio == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.greaterThanOrEqualTo(root.get("dataAbertura"), dataInicio);
    }

    public static Specification<Manutencao> comDataAberturaAte(LocalDateTime dataFim) {
        return (root, query, criteriaBuilder) -> dataFim == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.lessThanOrEqualTo(root.get("dataAbertura"), dataFim);
    }
}
