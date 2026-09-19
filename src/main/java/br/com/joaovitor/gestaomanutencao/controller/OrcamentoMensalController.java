package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.OrcamentoMensalRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.OrcamentoMensalResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.OrcamentoMensal;
import br.com.joaovitor.gestaomanutencao.repository.OrcamentoMensalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orcamentos-mensais")
public class OrcamentoMensalController {

    private final OrcamentoMensalRepository orcamentoMensalRepository;

    public OrcamentoMensalController(OrcamentoMensalRepository orcamentoMensalRepository) {
        this.orcamentoMensalRepository = orcamentoMensalRepository;
    }

    @PostMapping
    public ResponseEntity<OrcamentoMensalResponseDTO> criar(
            @RequestBody OrcamentoMensalRequestDTO requestDTO
    ) {
        if (orcamentoMensalRepository.findByMesAndAno(requestDTO.mes(), requestDTO.ano()).isPresent()) {
            throw new RuntimeException("Já existe orçamento cadastrado para este mês/ano.");
        }

        OrcamentoMensal orcamentoMensal = new OrcamentoMensal();
        orcamentoMensal.setMes(requestDTO.mes());
        orcamentoMensal.setAno(requestDTO.ano());
        orcamentoMensal.setValorPlanejado(requestDTO.valorPlanejado());

        OrcamentoMensal salvo = orcamentoMensalRepository.save(orcamentoMensal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrcamentoMensalResponseDTO.fromEntity(salvo));
    }

    @GetMapping("/{mes}/{ano}")
    public ResponseEntity<OrcamentoMensalResponseDTO> buscarPorMesEAno(
            @PathVariable Integer mes,
            @PathVariable Integer ano
    ) {
        return orcamentoMensalRepository.findByMesAndAno(mes, ano)
                .map(OrcamentoMensalResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{mes}/{ano}")
    public ResponseEntity<OrcamentoMensalResponseDTO> atualizar(
            @PathVariable Integer mes,
            @PathVariable Integer ano,
            @RequestBody OrcamentoMensalRequestDTO requestDTO
    ) {
        OrcamentoMensal orcamentoMensal = orcamentoMensalRepository.findByMesAndAno(mes, ano)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Orçamento mensal não encontrado para " + mes + "/" + ano + "."
                ));

        orcamentoMensal.setValorPlanejado(requestDTO.valorPlanejado());
        OrcamentoMensal atualizado = orcamentoMensalRepository.save(orcamentoMensal);

        return ResponseEntity.ok(OrcamentoMensalResponseDTO.fromEntity(atualizado));
    }
}
