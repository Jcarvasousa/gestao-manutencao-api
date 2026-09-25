package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.MaquinaRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.MaquinaResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.MaquinaStatusRequestDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.specification.MaquinaSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/maquinas")
public class MaquinaController {

    private final MaquinaRepository maquinaRepository;

    public MaquinaController(MaquinaRepository maquinaRepository) {
        this.maquinaRepository = maquinaRepository;
    }

    @PostMapping
    public ResponseEntity<MaquinaResponseDTO> criar(@Valid @RequestBody MaquinaRequestDTO requestDTO) {
        Maquina maquina = new Maquina();
        maquina.setCodigo(requestDTO.codigo());
        maquina.setDescricao(requestDTO.descricao());
        maquina.setSetor(requestDTO.setor());

        Maquina salva = maquinaRepository.save(maquina);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MaquinaResponseDTO.fromEntity(salva));
    }

    @GetMapping
    public ResponseEntity<Page<MaquinaResponseDTO>> listarTodas(
            Pageable pageable,
            @RequestParam(required = false) StatusMaquina status,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) String codigo
    ) {
        var specification = org.springframework.data.jpa.domain.Specification.where(
                MaquinaSpecification.comStatus(null)
        );
        if (status != null) {
            specification = specification.and(MaquinaSpecification.comStatus(status));
        }
        if (setor != null) {
            specification = specification.and(MaquinaSpecification.comSetor(setor));
        }
        if (codigo != null) {
            specification = specification.and(MaquinaSpecification.comCodigo(codigo));
        }

        return ResponseEntity.ok(
                maquinaRepository.findAll(specification, pageable).map(MaquinaResponseDTO::fromEntity)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaquinaResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<Maquina> maquinaOpt = maquinaRepository.findById(id);

        if (maquinaOpt.isPresent()) {
            return ResponseEntity.ok(MaquinaResponseDTO.fromEntity(maquinaOpt.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaquinaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaquinaRequestDTO requestDTO
    ) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + id + " não encontrada."
                ));

        maquina.setCodigo(requestDTO.codigo());
        maquina.setDescricao(requestDTO.descricao());
        maquina.setSetor(requestDTO.setor());

        Maquina atualizada = maquinaRepository.save(maquina);
        return ResponseEntity.ok(MaquinaResponseDTO.fromEntity(atualizada));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MaquinaResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody MaquinaStatusRequestDTO requestDTO
    ) {
        Maquina maquina = maquinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + id + " não encontrada."
                ));

        maquina.setStatus(requestDTO.status());

        Maquina atualizada = maquinaRepository.save(maquina);
        return ResponseEntity.ok(MaquinaResponseDTO.fromEntity(atualizada));
    }
}