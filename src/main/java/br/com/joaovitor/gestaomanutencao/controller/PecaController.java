package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.PecaRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.PecaResponseDTO;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private final PecaRepository pecaRepository;

    public PecaController(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @PostMapping
    public ResponseEntity<PecaResponseDTO> criar(@RequestBody PecaRequestDTO requestDTO) {
        Peca peca = new Peca();
        peca.setCodigo(requestDTO.codigo());
        peca.setNome(requestDTO.nome());
        peca.setCategoria(requestDTO.categoria());
        peca.setLocalizacaoFisica(requestDTO.localizacaoFisica());
        peca.setQuantidadeAtual(requestDTO.quantidadeAtual());
        peca.setEstoqueMinimo(requestDTO.estoqueMinimo());
        peca.setCustoUnitario(requestDTO.custoUnitario());

        Peca salva = pecaRepository.save(peca);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PecaResponseDTO.fromEntity(salva));
    }

    @GetMapping
    public ResponseEntity<List<PecaResponseDTO>> listarTodas() {
        List<PecaResponseDTO> pecas = pecaRepository.findAll()
                .stream()
                .map(PecaResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(pecas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<Peca> pecaOpt = pecaRepository.findById(id);

        if (pecaOpt.isPresent()) {
            return ResponseEntity.ok(PecaResponseDTO.fromEntity(pecaOpt.get()));
        }

        return ResponseEntity.notFound().build();
    }
}