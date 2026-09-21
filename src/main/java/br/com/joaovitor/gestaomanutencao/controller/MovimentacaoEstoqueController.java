package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoEntradaRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoEstoqueResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoSaidaRequestDTO;
import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.service.MovimentacaoEstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public MovimentacaoEstoqueController(MovimentacaoEstoqueService movimentacaoEstoqueService) {
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    @PostMapping("/saida")
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarSaida(@Valid @RequestBody MovimentacaoSaidaRequestDTO requestDTO) {
        MovimentacaoEstoque movimentacao = movimentacaoEstoqueService.registrarSaida(
                requestDTO.pecaId(),
                requestDTO.manutencaoId(),
                requestDTO.quantidade(),
                requestDTO.observacao()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacaoEstoqueResponseDTO.fromEntity(movimentacao));
    }

    @PostMapping("/entrada")
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarEntrada(@Valid @RequestBody MovimentacaoEntradaRequestDTO requestDTO) {
        MovimentacaoEstoque movimentacao = movimentacaoEstoqueService.registrarEntrada(
                requestDTO.pecaId(),
                requestDTO.quantidade(),
                requestDTO.observacao()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacaoEstoqueResponseDTO.fromEntity(movimentacao));
    }
}