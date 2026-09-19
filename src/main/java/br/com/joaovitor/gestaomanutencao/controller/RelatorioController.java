package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMensalDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinaDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MaquinaRepository maquinaRepository;

    public RelatorioController(
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            ManutencaoRepository manutencaoRepository,
            MaquinaRepository maquinaRepository
    ) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.maquinaRepository = maquinaRepository;
    }

    @GetMapping("/custo-mensal")
    public ResponseEntity<RelatorioCustoMensalDTO> custoMensal(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        BigDecimal custoPecas = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = manutencaoRepository.calcularCustoMaoDeObraPorMesEAno(mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        BigDecimal custoTotal = custoPecas.add(custoMaoDeObra);

        RelatorioCustoMensalDTO relatorio = new RelatorioCustoMensalDTO(
                mes,
                ano,
                custoPecas,
                custoMaoDeObra,
                custoTotal
        );

        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/custo-maquina/mensal")
    public ResponseEntity<RelatorioCustoMaquinaDTO> custoMaquinaMensal(
            @RequestParam Long maquinaId,
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        var maquina = maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + maquinaId + " não encontrada."
                ));

        BigDecimal custoPecas = movimentacaoEstoqueRepository
                .calcularCustoPecasPorMaquinaMesEAno(maquinaId, mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = manutencaoRepository
                .calcularCustoMaoDeObraPorMaquinaMesEAno(maquinaId, mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        return ResponseEntity.ok(new RelatorioCustoMaquinaDTO(
                maquinaId,
                maquina.getCodigo(),
                mes,
                ano,
                custoPecas,
                custoMaoDeObra,
                custoPecas.add(custoMaoDeObra)
        ));
    }

    @GetMapping("/custo-maquina/anual")
    public ResponseEntity<RelatorioCustoMaquinaDTO> custoMaquinaAnual(
            @RequestParam Long maquinaId,
            @RequestParam Integer ano
    ) {
        var maquina = maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + maquinaId + " não encontrada."
                ));

        BigDecimal custoPecas = movimentacaoEstoqueRepository
                .calcularCustoPecasPorMaquinaEAno(maquinaId, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = manutencaoRepository
                .calcularCustoMaoDeObraPorMaquinaEAno(maquinaId, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        return ResponseEntity.ok(new RelatorioCustoMaquinaDTO(
                maquinaId,
                maquina.getCodigo(),
                null,
                ano,
                custoPecas,
                custoMaoDeObra,
                custoPecas.add(custoMaoDeObra)
        ));
    }

    @GetMapping("/custo-maquina/total")
    public ResponseEntity<RelatorioCustoMaquinaDTO> custoMaquinaTotal(
            @RequestParam Long maquinaId
    ) {
        var maquina = maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Máquina com ID " + maquinaId + " não encontrada."
                ));

        BigDecimal custoPecas = movimentacaoEstoqueRepository
                .calcularCustoPecasPorMaquinaTotal(maquinaId);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = manutencaoRepository
                .calcularCustoMaoDeObraPorMaquinaTotal(maquinaId);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        return ResponseEntity.ok(new RelatorioCustoMaquinaDTO(
                maquinaId,
                maquina.getCodigo(),
                null,
                null,
                custoPecas,
                custoMaoDeObra,
                custoPecas.add(custoMaoDeObra)
        ));
    }
}
