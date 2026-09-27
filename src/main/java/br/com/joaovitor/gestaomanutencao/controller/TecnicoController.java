package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.TecnicoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.TecnicoResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Tecnico;
import br.com.joaovitor.gestaomanutencao.repository.TecnicoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoController(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    @PostMapping
    public ResponseEntity<TecnicoResponseDTO> criar(@Valid @RequestBody TecnicoRequestDTO requestDTO) {
        Tecnico tecnico = new Tecnico();
        tecnico.setNome(requestDTO.nome());
        tecnico.setSalarioMensal(requestDTO.salarioMensal());
        tecnico.setCargaHorariaDiaria(requestDTO.cargaHorariaDiaria());
        tecnico.setAtivo(requestDTO.ativo() == null ? Boolean.TRUE : requestDTO.ativo());

        Tecnico salvo = tecnicoRepository.save(tecnico);
        return ResponseEntity.status(HttpStatus.CREATED).body(TecnicoResponseDTO.fromEntity(salvo));
    }

    @GetMapping
    public ResponseEntity<Page<TecnicoResponseDTO>> listarTodos(Pageable pageable) {
        return ResponseEntity.ok(
                tecnicoRepository.findAll(pageable).map(TecnicoResponseDTO::fromEntity)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecnicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return tecnicoRepository.findById(id)
                .map(TecnicoResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TecnicoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TecnicoRequestDTO requestDTO
    ) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Técnico com ID " + id + " não encontrado."
                ));

        tecnico.setNome(requestDTO.nome());
        tecnico.setSalarioMensal(requestDTO.salarioMensal());
        tecnico.setCargaHorariaDiaria(requestDTO.cargaHorariaDiaria());
        tecnico.setAtivo(requestDTO.ativo() == null ? Boolean.TRUE : requestDTO.ativo());

        Tecnico atualizado = tecnicoRepository.save(tecnico);
        return ResponseEntity.ok(TecnicoResponseDTO.fromEntity(atualizado));
    }
}
