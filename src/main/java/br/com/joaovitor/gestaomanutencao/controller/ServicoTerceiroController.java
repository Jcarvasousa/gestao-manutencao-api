package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ServicoTerceiroAtualizacaoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ServicoTerceiroRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ServicoTerceiroResponseDTO;
import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import br.com.joaovitor.gestaomanutencao.service.ServicoTerceiroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicos-terceiros")
public class ServicoTerceiroController {

    private final ServicoTerceiroRepository servicoTerceiroRepository;
    private final ServicoTerceiroService servicoTerceiroService;

    public ServicoTerceiroController(
            ServicoTerceiroRepository servicoTerceiroRepository,
            ServicoTerceiroService servicoTerceiroService
    ) {
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.servicoTerceiroService = servicoTerceiroService;
    }

    @PostMapping
    public ResponseEntity<ServicoTerceiroResponseDTO> criar(@Valid @RequestBody ServicoTerceiroRequestDTO requestDTO) {
        ServicoTerceiro salvo = servicoTerceiroService.criar(
                requestDTO.manutencaoId(),
                requestDTO.valorApurado(),
                requestDTO.horasTrabalhadas(),
                requestDTO.valorHora(),
                requestDTO.descricao(),
                requestDTO.fornecedor()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicoTerceiroResponseDTO.fromEntity(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoTerceiroResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoTerceiroAtualizacaoRequestDTO requestDTO
    ) {
        ServicoTerceiro atualizado = servicoTerceiroService.atualizar(
                id,
                requestDTO.valorFinal(),
                requestDTO.observacao()
        );
        return ResponseEntity.ok(ServicoTerceiroResponseDTO.fromEntity(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servicoTerceiroService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ServicoTerceiroResponseDTO>> listarTodos(
            Pageable pageable,
            @RequestParam(required = false) Long manutencaoId
    ) {
        Page<ServicoTerceiro> pagina = manutencaoId != null
                ? servicoTerceiroRepository.findByManutencaoId(manutencaoId, pageable)
                : servicoTerceiroRepository.findAll(pageable);

        return ResponseEntity.ok(pagina.map(ServicoTerceiroResponseDTO::fromEntity));
    }
}
