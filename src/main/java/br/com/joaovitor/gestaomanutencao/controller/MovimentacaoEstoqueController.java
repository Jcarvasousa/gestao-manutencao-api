package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoAjusteRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoDevolucaoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoEntradaRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoEstoqueResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.MovimentacaoSaidaRequestDTO;
import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.service.MovimentacaoEstoqueService;
import br.com.joaovitor.gestaomanutencao.specification.MovimentacaoEstoqueSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public MovimentacaoEstoqueController(
            MovimentacaoEstoqueService movimentacaoEstoqueService,
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository
    ) {
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    @GetMapping
    public ResponseEntity<Page<MovimentacaoEstoqueResponseDTO>> listarTodas(
            Pageable pageable,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) Long pecaId,
            @RequestParam(required = false) Long manutencaoId
    ) {
        var specification = org.springframework.data.jpa.domain.Specification.where(
                MovimentacaoEstoqueSpecification.comTipo(null)
        );
        if (tipo != null) {
            specification = specification.and(MovimentacaoEstoqueSpecification.comTipo(tipo));
        }
        if (pecaId != null) {
            specification = specification.and(MovimentacaoEstoqueSpecification.comPecaId(pecaId));
        }
        if (manutencaoId != null) {
            specification = specification.and(MovimentacaoEstoqueSpecification.comManutencaoId(manutencaoId));
        }

        return ResponseEntity.ok(
                movimentacaoEstoqueRepository.findAll(specification, pageable)
                        .map(MovimentacaoEstoqueResponseDTO::fromEntity)
        );
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

    @PostMapping("/devolucao")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> registrarDevolucao(@Valid @RequestBody MovimentacaoDevolucaoRequestDTO requestDTO) {
        List<MovimentacaoEstoqueResponseDTO> devolucoes = movimentacaoEstoqueService.registrarDevolucao(
                        requestDTO.pecaId(),
                        requestDTO.manutencaoId(),
                        requestDTO.quantidade(),
                        requestDTO.observacao()
                )
                .stream()
                .map(MovimentacaoEstoqueResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(devolucoes);
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

    @PostMapping("/ajuste")
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarAjuste(@Valid @RequestBody MovimentacaoAjusteRequestDTO requestDTO) {
        MovimentacaoEstoque movimentacao = movimentacaoEstoqueService.registrarAjuste(
                requestDTO.pecaId(),
                requestDTO.quantidadeNova(),
                requestDTO.observacao()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacaoEstoqueResponseDTO.fromEntity(movimentacao));
    }
}