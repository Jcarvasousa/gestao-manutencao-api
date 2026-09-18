package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ManutencaoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manutencoes")
public class ManutencaoController {

    private final ManutencaoRepository manutencaoRepository;
    private final MaquinaRepository maquinaRepository;

    public ManutencaoController(ManutencaoRepository manutencaoRepository, MaquinaRepository maquinaRepository) {
        this.manutencaoRepository = manutencaoRepository;
        this.maquinaRepository = maquinaRepository;
    }

    @PostMapping
    public ResponseEntity<ManutencaoResponseDTO> criar(@RequestBody ManutencaoRequestDTO requestDTO) {
        Maquina maquina = maquinaRepository.findById(requestDTO.maquinaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + requestDTO.maquinaId() + " não encontrada."
                ));

        Manutencao manutencao = new Manutencao();
        manutencao.setMaquina(maquina);
        manutencao.setProblemaDescricao(requestDTO.problemaDescricao());
        manutencao.setTecnicoResponsavel(requestDTO.tecnicoResponsavel());

        Manutencao salva = manutencaoRepository.save(manutencao);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ManutencaoResponseDTO.fromEntity(salva));
    }

    @GetMapping
    public ResponseEntity<List<ManutencaoResponseDTO>> listarTodas() {
        List<ManutencaoResponseDTO> lista = manutencaoRepository.findAll()
                .stream()
                .map(ManutencaoResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManutencaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return manutencaoRepository.findById(id)
                .map(ManutencaoResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}