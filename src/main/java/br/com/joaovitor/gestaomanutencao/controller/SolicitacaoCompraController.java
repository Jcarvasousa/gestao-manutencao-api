package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.SolicitacaoCompraRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.SolicitacaoCompraResponseDTO;
import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.service.SolicitacaoCompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitacoes-compra")
public class SolicitacaoCompraController {

    private final SolicitacaoCompraService solicitacaoCompraService;

    public SolicitacaoCompraController(SolicitacaoCompraService solicitacaoCompraService) {
        this.solicitacaoCompraService = solicitacaoCompraService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoCompraResponseDTO> criar(@RequestBody SolicitacaoCompraRequestDTO requestDTO) {
        SolicitacaoCompra solicitacaoCompra = solicitacaoCompraService.criar(
                requestDTO.pecaId(),
                requestDTO.manutencaoId(),
                requestDTO.quantidade(),
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
