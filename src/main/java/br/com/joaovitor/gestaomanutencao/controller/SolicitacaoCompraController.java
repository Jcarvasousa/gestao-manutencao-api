package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.SolicitacaoCompraRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.SolicitacaoCompraResponseDTO;
import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.repository.SolicitacaoCompraRepository;
import br.com.joaovitor.gestaomanutencao.service.SolicitacaoCompraService;
import br.com.joaovitor.gestaomanutencao.specification.SolicitacaoCompraSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/solicitacoes-compra")
public class SolicitacaoCompraController {

    private final SolicitacaoCompraService solicitacaoCompraService;
    private final SolicitacaoCompraRepository solicitacaoCompraRepository;

    public SolicitacaoCompraController(
            SolicitacaoCompraService solicitacaoCompraService,
            SolicitacaoCompraRepository solicitacaoCompraRepository
    ) {
        this.solicitacaoCompraService = solicitacaoCompraService;
        this.solicitacaoCompraRepository = solicitacaoCompraRepository;
    }

    @GetMapping
    public ResponseEntity<Page<SolicitacaoCompraResponseDTO>> listarTodas(
            Pageable pageable,
            @RequestParam(required = false) StatusSolicitacaoCompra status,
            @RequestParam(required = false) Long pecaId
    ) {
        var specification = org.springframework.data.jpa.domain.Specification.where(
                SolicitacaoCompraSpecification.comStatus(null)
        );
        if (status != null) {
            specification = specification.and(SolicitacaoCompraSpecification.comStatus(status));
        }
        if (pecaId != null) {
            specification = specification.and(SolicitacaoCompraSpecification.comPecaId(pecaId));
        }

        return ResponseEntity.ok(
                solicitacaoCompraRepository.findAll(specification, pageable)
                        .map(SolicitacaoCompraResponseDTO::fromEntity)
        );
    }

    @PostMapping
    public ResponseEntity<SolicitacaoCompraResponseDTO> criar(@Valid @RequestBody SolicitacaoCompraRequestDTO requestDTO) {
        SolicitacaoCompra solicitacaoCompra = solicitacaoCompraService.criar(
                requestDTO.pecaId(),
                requestDTO.manutencaoId(),
                requestDTO.quantidadeNecessaria(),
                requestDTO.fornecedor(),
                requestDTO.valorOrcamento()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SolicitacaoCompraResponseDTO.fromEntity(solicitacaoCompra));
    }

    @PatchMapping("/{id}/receber")
    public ResponseEntity<SolicitacaoCompraResponseDTO> receber(@PathVariable Long id) {
        SolicitacaoCompra solicitacaoCompra = solicitacaoCompraService.marcarComoRecebida(id);

        return ResponseEntity.ok(SolicitacaoCompraResponseDTO.fromEntity(solicitacaoCompra));
    }
}