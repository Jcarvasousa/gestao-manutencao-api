package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ManutencaoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoConcluirRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.specification.ManutencaoSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    public ResponseEntity<ManutencaoResponseDTO> criar(@Valid @RequestBody ManutencaoRequestDTO requestDTO) {
        Maquina maquina = maquinaRepository.findById(requestDTO.maquinaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + requestDTO.maquinaId() + " não encontrada."
                ));

        Manutencao manutencao = new Manutencao();
        manutencao.setMaquina(maquina);
        manutencao.setProblemaDescricao(requestDTO.problemaDescricao());
        manutencao.setTipo(requestDTO.tipo());
        manutencao.setTecnicoResponsavel(requestDTO.tecnicoResponsavel());

        Manutencao salva = manutencaoRepository.save(manutencao);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ManutencaoResponseDTO.fromEntity(salva));
    }

    @GetMapping
    public ResponseEntity<Page<ManutencaoResponseDTO>> listarTodas(
            Pageable pageable,
            @RequestParam(required = false) StatusManutencao status,
            @RequestParam(required = false) TipoManutencao tipo,
            @RequestParam(required = false) Long maquinaId,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim
    ) {
        var specification = org.springframework.data.jpa.domain.Specification.where(
                ManutencaoSpecification.comStatus(null)
        );
        if (status != null) {
            specification = specification.and(ManutencaoSpecification.comStatus(status));
        }
        if (tipo != null) {
            specification = specification.and(ManutencaoSpecification.comTipo(tipo));
        }
        if (maquinaId != null) {
            specification = specification.and(ManutencaoSpecification.comMaquinaId(maquinaId));
        }
        if (dataInicio != null) {
            specification = specification.and(ManutencaoSpecification.comDataAberturaDesde(dataInicio));
        }
        if (dataFim != null) {
            specification = specification.and(ManutencaoSpecification.comDataAberturaAte(dataFim));
        }

        return ResponseEntity.ok(
                manutencaoRepository.findAll(specification, pageable).map(ManutencaoResponseDTO::fromEntity)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManutencaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return manutencaoRepository.findById(id)
                .map(ManutencaoResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<ManutencaoResponseDTO> iniciar(@PathVariable Long id) {
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Manutenção com ID " + id + " não encontrada."
                ));

        if (manutencao.getStatus() != StatusManutencao.ABERTA) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Só é possível iniciar uma manutenção que está ABERTA."
            );
        }

        manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
        manutencao.setDataInicio(LocalDateTime.now());

        Manutencao atualizada = manutencaoRepository.save(manutencao);
        return ResponseEntity.ok(ManutencaoResponseDTO.fromEntity(atualizada));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ManutencaoResponseDTO> concluir(
            @PathVariable Long id,
            @RequestBody ManutencaoConcluirRequestDTO requestDTO
    ) {
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Manutenção com ID " + id + " não encontrada."
                ));

        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA
                || manutencao.getStatus() == StatusManutencao.CANCELADA) {
            throw new ManutencaoNaoEstaAbertaException(
                    "A manutenção já está finalizada."
            );
        }

        manutencao.setDescricaoServico(requestDTO.descricaoServico());
        manutencao.setCustoMaoDeObra(requestDTO.custoMaoDeObra() == null
                ? BigDecimal.ZERO
                : requestDTO.custoMaoDeObra());
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDataConclusao(LocalDateTime.now());

        Manutencao atualizada = manutencaoRepository.save(manutencao);
        return ResponseEntity.ok(ManutencaoResponseDTO.fromEntity(atualizada));
    }
}