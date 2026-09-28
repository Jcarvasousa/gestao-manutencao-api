package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.ManutencaoRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoConcluirRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.ManutencaoResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.PecaUsadaResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoDadosConclusaoIncompletosException;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoSemResponsavelException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoTecnicoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import br.com.joaovitor.gestaomanutencao.service.ManutencaoService;
import br.com.joaovitor.gestaomanutencao.service.MovimentacaoEstoqueService;
import br.com.joaovitor.gestaomanutencao.specification.ManutencaoSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/manutencoes")
public class ManutencaoController {

    private final ManutencaoRepository manutencaoRepository;
    private final MaquinaRepository maquinaRepository;
    private final ServicoTerceiroRepository servicoTerceiroRepository;
    private final ManutencaoTecnicoRepository manutencaoTecnicoRepository;
    private final ManutencaoService manutencaoService;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public ManutencaoController(
            ManutencaoRepository manutencaoRepository,
            MaquinaRepository maquinaRepository,
            ServicoTerceiroRepository servicoTerceiroRepository,
            ManutencaoTecnicoRepository manutencaoTecnicoRepository,
            ManutencaoService manutencaoService,
            MovimentacaoEstoqueService movimentacaoEstoqueService
    ) {
        this.manutencaoRepository = manutencaoRepository;
        this.maquinaRepository = maquinaRepository;
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.manutencaoTecnicoRepository = manutencaoTecnicoRepository;
        this.manutencaoService = manutencaoService;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
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

        manutencaoRepository.save(manutencao);
        return ResponseEntity.ok(ManutencaoResponseDTO.fromEntity(manutencao));
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

        if (manutencao.estaFinalizada()) {
            throw new ManutencaoNaoEstaAbertaException(
                    "A manutenção já está finalizada."
            );
        }

        if (requestDTO.descricaoServico() == null || requestDTO.descricaoServico().isBlank()) {
            throw new ManutencaoDadosConclusaoIncompletosException(
                    "A descrição do serviço executado é obrigatória para concluir a manutenção."
            );
        }

        if (requestDTO.maquinaLiberadaParaUso() == null) {
            throw new ManutencaoDadosConclusaoIncompletosException(
                    "É obrigatório informar se a máquina foi liberada para uso para concluir a manutenção."
            );
        }

        if (requestDTO.condicoesSeguranca() == null || requestDTO.condicoesSeguranca().isBlank()) {
            throw new ManutencaoDadosConclusaoIncompletosException(
                    "As condições de segurança (NR12) são obrigatórias para concluir a manutenção."
            );
        }

        boolean temTecnico = manutencaoTecnicoRepository.existsByManutencaoId(id);
        boolean temServicoTerceiro = servicoTerceiroRepository.existsByManutencaoId(id);

        if (!temTecnico && !temServicoTerceiro) {
            throw new ManutencaoSemResponsavelException(
                    "A manutenção precisa de um técnico responsável ou de um serviço de terceiro vinculado para ser concluída."
            );
        }

        manutencao.setDescricaoServico(requestDTO.descricaoServico());
        manutencao.setMaquinaLiberadaParaUso(requestDTO.maquinaLiberadaParaUso());
        manutencao.setCondicoesSeguranca(requestDTO.condicoesSeguranca());
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDataConclusao(LocalDateTime.now());

        manutencaoRepository.save(manutencao);
        return ResponseEntity.ok(ManutencaoResponseDTO.fromEntity(manutencao));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ManutencaoResponseDTO> cancelar(@PathVariable Long id) {
        Manutencao manutencao = manutencaoService.cancelar(id);
        return ResponseEntity.ok(ManutencaoResponseDTO.fromEntity(manutencao));
    }

    @GetMapping("/{id}/pecas-usadas")
    public ResponseEntity<List<PecaUsadaResponseDTO>> listarPecasUsadas(@PathVariable Long id) {
        return ResponseEntity.ok(movimentacaoEstoqueService.listarPecasUsadas(id));
    }
}
