package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ManutencaoTecnicoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoTecnicoResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoTecnicoRepository;
import br.com.joaovitor.gestaomanutencao.service.ManutencaoTecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manutencoes/{manutencaoId}/tecnicos")
public class ManutencaoTecnicoController {

    private final ManutencaoTecnicoRepository manutencaoTecnicoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final ManutencaoTecnicoService manutencaoTecnicoService;

    public ManutencaoTecnicoController(
            ManutencaoTecnicoRepository manutencaoTecnicoRepository,
            ManutencaoRepository manutencaoRepository,
            ManutencaoTecnicoService manutencaoTecnicoService
    ) {
        this.manutencaoTecnicoRepository = manutencaoTecnicoRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.manutencaoTecnicoService = manutencaoTecnicoService;
    }

    @PostMapping
    public ResponseEntity<ManutencaoTecnicoResponseDTO> criar(
            @PathVariable Long manutencaoId,
            @Valid @RequestBody ManutencaoTecnicoRequestDTO requestDTO
    ) {
        ManutencaoTecnico manutencaoTecnico = manutencaoTecnicoService.criar(
                manutencaoId,
                requestDTO.tecnicoId(),
                requestDTO.horasTrabalhadas()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ManutencaoTecnicoResponseDTO.fromEntity(manutencaoTecnico));
    }

    @DeleteMapping("/{vinculoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long manutencaoId, @PathVariable Long vinculoId) {
        manutencaoTecnicoService.excluir(manutencaoId, vinculoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ManutencaoTecnicoResponseDTO>> listar(@PathVariable Long manutencaoId) {
        if (!manutencaoRepository.existsById(manutencaoId)) {
            throw new RecursoNaoEncontradoException(
                    "Manutenção com ID " + manutencaoId + " não encontrada."
            );
        }

        List<ManutencaoTecnicoResponseDTO> vinculos = manutencaoTecnicoRepository.findByManutencaoId(manutencaoId)
                .stream()
                .map(ManutencaoTecnicoResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(vinculos);
    }
}
