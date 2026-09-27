package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ServicoTerceiroRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ServicoTerceiroResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
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
    private final ManutencaoRepository manutencaoRepository;

    public ServicoTerceiroController(
            ServicoTerceiroRepository servicoTerceiroRepository,
            ManutencaoRepository manutencaoRepository
    ) {
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    @PostMapping
    public ResponseEntity<ServicoTerceiroResponseDTO> criar(@Valid @RequestBody ServicoTerceiroRequestDTO requestDTO) {
        Manutencao manutencao = manutencaoRepository.findById(requestDTO.manutencaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Manutenção com ID " + requestDTO.manutencaoId() + " não encontrada."
                ));

        ServicoTerceiro servicoTerceiro = new ServicoTerceiro();
        servicoTerceiro.setManutencao(manutencao);
        servicoTerceiro.setValor(requestDTO.valor());
        servicoTerceiro.setDescricao(requestDTO.descricao());
        servicoTerceiro.setFornecedor(requestDTO.fornecedor());

        ServicoTerceiro salvo = servicoTerceiroRepository.save(servicoTerceiro);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicoTerceiroResponseDTO.fromEntity(salvo));
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
