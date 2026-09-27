package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ManutencaoTecnicoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoTecnicoResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;
import br.com.joaovitor.gestaomanutencao.model.Tecnico;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoTecnicoRepository;
import br.com.joaovitor.gestaomanutencao.repository.TecnicoRepository;
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
    private final TecnicoRepository tecnicoRepository;

    public ManutencaoTecnicoController(
            ManutencaoTecnicoRepository manutencaoTecnicoRepository,
            ManutencaoRepository manutencaoRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.manutencaoTecnicoRepository = manutencaoTecnicoRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @PostMapping
    public ResponseEntity<ManutencaoTecnicoResponseDTO> criar(
            @PathVariable Long manutencaoId,
            @Valid @RequestBody ManutencaoTecnicoRequestDTO requestDTO
    ) {
        Manutencao manutencao = manutencaoRepository.findById(manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Manutenção com ID " + manutencaoId + " não encontrada."
                ));

        Tecnico tecnico = tecnicoRepository.findById(requestDTO.tecnicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Técnico com ID " + requestDTO.tecnicoId() + " não encontrado."
                ));

        ManutencaoTecnico manutencaoTecnico = new ManutencaoTecnico();
        manutencaoTecnico.setManutencao(manutencao);
        manutencaoTecnico.setTecnico(tecnico);
        manutencaoTecnico.setHorasTrabalhadas(requestDTO.horasTrabalhadas());

        manutencaoTecnicoRepository.save(manutencaoTecnico);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ManutencaoTecnicoResponseDTO.fromEntity(manutencaoTecnico));
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
