package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.PecaRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.PecaResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private final PecaRepository pecaRepository;

    public PecaController(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @PostMapping
    public ResponseEntity<PecaResponseDTO> criar(@Valid @RequestBody PecaRequestDTO requestDTO) {
        Peca peca = new Peca();
        peca.setCodigo(requestDTO.codigo());
        peca.setNome(requestDTO.nome());
        peca.setCategoria(requestDTO.categoria());
        peca.setUnidadeMedida(requestDTO.unidadeMedida());
        peca.setLocalizacaoFisica(requestDTO.localizacaoFisica());
        peca.setQuantidadeAtual(requestDTO.quantidadeAtual());
        peca.setEstoqueMinimo(requestDTO.estoqueMinimo());
        peca.setCustoUnitario(requestDTO.custoUnitario());

        Peca salva = pecaRepository.save(peca);
        return ResponseEntity.status(HttpStatus.CREATED).body(PecaResponseDTO.fromEntity(salva));
    }

    @GetMapping
    public ResponseEntity<List<PecaResponseDTO>> listarTodas() {
        List<PecaResponseDTO> lista = pecaRepository.findAll()
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/abaixo-do-minimo")
    public ResponseEntity<List<PecaResponseDTO>> listarAbaixoDoMinimo() {
        List<PecaResponseDTO> lista = pecaRepository.findAll()
                .stream()
                .filter(peca -> peca.getEstoqueMinimo() != null)
                .filter(peca -> peca.getQuantidadeAtual() < peca.getEstoqueMinimo())
                .map(PecaResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> buscarPorId(@PathVariable Long id) {
        return pecaRepository.findById(id)
                .map(PecaResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PecaRequestDTO requestDTO
    ) {
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Peça com ID " + id + " não encontrada."
                ));

        peca.setCodigo(requestDTO.codigo());
        peca.setNome(requestDTO.nome());
        peca.setCategoria(requestDTO.categoria());
        peca.setUnidadeMedida(requestDTO.unidadeMedida());
        peca.setLocalizacaoFisica(requestDTO.localizacaoFisica());
        peca.setQuantidadeAtual(requestDTO.quantidadeAtual());
        peca.setEstoqueMinimo(requestDTO.estoqueMinimo());
        peca.setCustoUnitario(requestDTO.custoUnitario());

        Peca atualizada = pecaRepository.save(peca);
        return ResponseEntity.ok(PecaResponseDTO.fromEntity(atualizada));
    }
}