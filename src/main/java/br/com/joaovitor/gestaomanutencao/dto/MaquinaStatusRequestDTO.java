package br.com.joaovitor.gestaomanutencao.dto;

import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
import jakarta.validation.constraints.NotNull;

public record MaquinaStatusRequestDTO(
        @NotNull
        StatusMaquina status
) {
}
