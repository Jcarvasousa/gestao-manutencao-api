package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMensalDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinaDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinasResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetoresResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioGastoRealizadoDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioKpisDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoAnualDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoMensalDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.RelatorioParametrosInvalidosException;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.pdf.RelatorioPdfBuilder;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.repository.OrcamentoMensalRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import br.com.joaovitor.gestaomanutencao.repository.SolicitacaoCompraRepository;
import br.com.joaovitor.gestaomanutencao.service.CustoManutencaoService;
import br.com.joaovitor.gestaomanutencao.service.CustoSetorService;
import org.openpdf.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ServicoTerceiroRepository servicoTerceiroRepository;
    private final MaquinaRepository maquinaRepository;
    private final SolicitacaoCompraRepository solicitacaoCompraRepository;
    private final OrcamentoMensalRepository orcamentoMensalRepository;
    private final CustoManutencaoService custoManutencaoService;
    private final ManutencaoRepository manutencaoRepository;
    private final CustoSetorService custoSetorService;
    private final RelatorioPdfBuilder relatorioPdfBuilder;

    public RelatorioController(
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            ServicoTerceiroRepository servicoTerceiroRepository,
            MaquinaRepository maquinaRepository,
            SolicitacaoCompraRepository solicitacaoCompraRepository,
            OrcamentoMensalRepository orcamentoMensalRepository,
            CustoManutencaoService custoManutencaoService,
            ManutencaoRepository manutencaoRepository,
            CustoSetorService custoSetorService,
            RelatorioPdfBuilder relatorioPdfBuilder
    ) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.maquinaRepository = maquinaRepository;
        this.solicitacaoCompraRepository = solicitacaoCompraRepository;
        this.orcamentoMensalRepository = orcamentoMensalRepository;
        this.custoManutencaoService = custoManutencaoService;
        this.manutencaoRepository = manutencaoRepository;
        this.custoSetorService = custoSetorService;
        this.relatorioPdfBuilder = relatorioPdfBuilder;
    }

    @GetMapping("/kpis")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioKpisDTO> kpis() {
        Long backlogQuantidade = manutencaoRepository.contarBacklog();

        Double mttrHorasBruto = manutencaoRepository.calcularMttrHoras();
        BigDecimal mttrHoras = mttrHorasBruto == null
                ? null
                : BigDecimal.valueOf(mttrHorasBruto).setScale(2, RoundingMode.HALF_UP);

        return ResponseEntity.ok(new RelatorioKpisDTO(backlogQuantidade, mttrHoras));
    }

    @GetMapping("/custo-mensal")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioCustoMensalDTO> custoMensal(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        BigDecimal custoPecas = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = custoManutencaoService
                .calcularCustoMaoDeObraPorMesEAno(mes, ano);
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

    @GetMapping("/custo-mensal/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoMensalPdf(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) throws DocumentException {
        BigDecimal custoPecas = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = custoManutencaoService
                .calcularCustoMaoDeObraPorMesEAno(mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        BigDecimal custoTotal = custoPecas.add(custoMaoDeObra);

        RelatorioCustoMensalDTO relatorio = new RelatorioCustoMensalDTO(
                mes, ano, custoPecas, custoMaoDeObra, custoTotal
        );
        byte[] pdf = relatorioPdfBuilder.custoMensal(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-custo-mensal.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/orcamento-mensal")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioOrcamentoMensalDTO> orcamentoMensal(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        var orcamentoMensal = orcamentoMensalRepository.findByMesAndAno(mes, ano)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Não há orçamento planejado cadastrado para " + mes + "/" + ano + "."
                ));

        BigDecimal valorPlanejado = orcamentoMensal.getValorPlanejado();
        BigDecimal valorRealizado = calcularValorRealizadoMensal(mes, ano);

        BigDecimal saldoDisponivel = valorPlanejado.subtract(valorRealizado);
        BigDecimal percentualUtilizado = valorPlanejado.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : valorRealizado
                .divide(valorPlanejado, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return ResponseEntity.ok(new RelatorioOrcamentoMensalDTO(
                mes,
                ano,
                valorPlanejado,
                valorRealizado,
                saldoDisponivel,
                percentualUtilizado
        ));
    }

    @GetMapping("/orcamento-mensal/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> orcamentoMensalPdf(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) throws DocumentException {
        var orcamentoMensal = orcamentoMensalRepository.findByMesAndAno(mes, ano)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Não há orçamento planejado cadastrado para " + mes + "/" + ano + "."
                ));

        BigDecimal valorPlanejado = orcamentoMensal.getValorPlanejado();
        BigDecimal valorRealizado = calcularValorRealizadoMensal(mes, ano);

        BigDecimal saldoDisponivel = valorPlanejado.subtract(valorRealizado);
        BigDecimal percentualUtilizado = valorPlanejado.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : valorRealizado
                .divide(valorPlanejado, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        RelatorioOrcamentoMensalDTO relatorio = new RelatorioOrcamentoMensalDTO(
                mes, ano, valorPlanejado, valorRealizado, saldoDisponivel, percentualUtilizado
        );
        byte[] pdf = relatorioPdfBuilder.orcamentoMensal(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-orcamento-mensal.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/orcamento-anual")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioOrcamentoAnualDTO> orcamentoAnual(
            @RequestParam Integer ano
    ) {
        List<br.com.joaovitor.gestaomanutencao.model.OrcamentoMensal> orcamentos =
                orcamentoMensalRepository.findByAno(ano);
        BigDecimal valorPlanejadoTotal = orcamentos.stream()
                .map(orcamento -> orcamento.getValorPlanejado())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorRealizadoTotal = calcularValorRealizadoAnual(ano);

        BigDecimal saldoDisponivel = valorPlanejadoTotal.subtract(valorRealizadoTotal);
        BigDecimal percentualUtilizado = valorPlanejadoTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : valorRealizadoTotal
                .divide(valorPlanejadoTotal, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return ResponseEntity.ok(new RelatorioOrcamentoAnualDTO(
                ano,
                valorPlanejadoTotal,
                valorRealizadoTotal,
                saldoDisponivel,
                percentualUtilizado
        ));
    }

    @GetMapping("/orcamento-anual/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> orcamentoAnualPdf(
            @RequestParam Integer ano
    ) throws DocumentException {
        List<br.com.joaovitor.gestaomanutencao.model.OrcamentoMensal> orcamentos =
                orcamentoMensalRepository.findByAno(ano);
        BigDecimal valorPlanejadoTotal = orcamentos.stream()
                .map(orcamento -> orcamento.getValorPlanejado())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorRealizadoTotal = calcularValorRealizadoAnual(ano);

        BigDecimal saldoDisponivel = valorPlanejadoTotal.subtract(valorRealizadoTotal);
        BigDecimal percentualUtilizado = valorPlanejadoTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : valorRealizadoTotal
                .divide(valorPlanejadoTotal, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        RelatorioOrcamentoAnualDTO relatorio = new RelatorioOrcamentoAnualDTO(
                ano, valorPlanejadoTotal, valorRealizadoTotal, saldoDisponivel, percentualUtilizado
        );
        byte[] pdf = relatorioPdfBuilder.orcamentoAnual(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-orcamento-anual.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/gasto-realizado")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioGastoRealizadoDTO> gastoRealizado(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        BigDecimal valorGastoPecas = solicitacaoCompraRepository.calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorGastoPecas == null) valorGastoPecas = BigDecimal.ZERO;

        BigDecimal valorGastoServicoTerceiro = servicoTerceiroRepository.somarValorServicoTerceiroPorMesEAno(mes, ano);
        if (valorGastoServicoTerceiro == null) valorGastoServicoTerceiro = BigDecimal.ZERO;

        BigDecimal valorGastoTotal = valorGastoPecas.add(valorGastoServicoTerceiro);

        return ResponseEntity.ok(new RelatorioGastoRealizadoDTO(
                mes, ano, valorGastoPecas, valorGastoServicoTerceiro, valorGastoTotal
        ));
    }

    @GetMapping("/gasto-realizado/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> gastoRealizadoPdf(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) throws DocumentException {
        BigDecimal valorGastoPecas = solicitacaoCompraRepository.calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorGastoPecas == null) valorGastoPecas = BigDecimal.ZERO;

        BigDecimal valorGastoServicoTerceiro = servicoTerceiroRepository.somarValorServicoTerceiroPorMesEAno(mes, ano);
        if (valorGastoServicoTerceiro == null) valorGastoServicoTerceiro = BigDecimal.ZERO;

        BigDecimal valorGastoTotal = valorGastoPecas.add(valorGastoServicoTerceiro);

        RelatorioGastoRealizadoDTO relatorio = new RelatorioGastoRealizadoDTO(
                mes, ano, valorGastoPecas, valorGastoServicoTerceiro, valorGastoTotal
        );
        byte[] pdf = relatorioPdfBuilder.gastoRealizado(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-gasto-realizado.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/custo-maquina/mensal")
    @Transactional(readOnly = true)
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

        BigDecimal custoMaoDeObra = custoManutencaoService
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

    @GetMapping("/custo-maquina/mensal/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoMaquinaMensalPdf(
            @RequestParam Long maquinaId,
                @RequestParam Integer mes,
                @RequestParam Integer ano
    ) throws DocumentException {
        var maquina = maquinaRepository.findById(maquinaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Máquina com ID " + maquinaId + " não encontrada."
                    ));

        BigDecimal custoPecas = movimentacaoEstoqueRepository
                    .calcularCustoPecasPorMaquinaMesEAno(maquinaId, mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = custoManutencaoService
                .calcularCustoMaoDeObraPorMaquinaMesEAno(maquinaId, mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        RelatorioCustoMaquinaDTO relatorio = new RelatorioCustoMaquinaDTO(
                maquinaId, maquina.getCodigo(), mes, ano,
                custoPecas, custoMaoDeObra, custoPecas.add(custoMaoDeObra)
        );
        byte[] pdf = relatorioPdfBuilder.custoMaquinaMensal(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=relatorio-custo-maquina-mensal.pdf");

        return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
    }

    @GetMapping("/custo-maquina/anual")
    @Transactional(readOnly = true)
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

        BigDecimal custoMaoDeObra = custoManutencaoService
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

    @GetMapping("/custo-maquina/anual/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoMaquinaAnualPdf(
                @RequestParam Long maquinaId,
                @RequestParam Integer ano
    ) throws DocumentException {
        var maquina = maquinaRepository.findById(maquinaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Máquina com ID " + maquinaId + " não encontrada."
                    ));

        BigDecimal custoPecas = movimentacaoEstoqueRepository
                    .calcularCustoPecasPorMaquinaEAno(maquinaId, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = custoManutencaoService
                .calcularCustoMaoDeObraPorMaquinaEAno(maquinaId, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        RelatorioCustoMaquinaDTO relatorio = new RelatorioCustoMaquinaDTO(
                maquinaId, maquina.getCodigo(), null, ano,
                custoPecas, custoMaoDeObra, custoPecas.add(custoMaoDeObra)
        );
        byte[] pdf = relatorioPdfBuilder.custoMaquinaAnual(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=relatorio-custo-maquina-anual.pdf");

        return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
    }

    @GetMapping("/custo-maquina/total")
    @Transactional(readOnly = true)
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

        BigDecimal custoMaoDeObra = custoManutencaoService
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

    @GetMapping("/custo-maquina/total/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoMaquinaTotalPdf(
                @RequestParam Long maquinaId
    ) throws DocumentException {
        var maquina = maquinaRepository.findById(maquinaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Máquina com ID " + maquinaId + " não encontrada."
                    ));

        BigDecimal custoPecas = movimentacaoEstoqueRepository
                    .calcularCustoPecasPorMaquinaTotal(maquinaId);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = custoManutencaoService
                .calcularCustoMaoDeObraPorMaquinaTotal(maquinaId);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        RelatorioCustoMaquinaDTO relatorio = new RelatorioCustoMaquinaDTO(
                maquinaId, maquina.getCodigo(), null, null,
                custoPecas, custoMaoDeObra, custoPecas.add(custoMaoDeObra)
        );
        byte[] pdf = relatorioPdfBuilder.custoMaquinaTotal(relatorio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=relatorio-custo-maquina-total.pdf");

        return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
    }

    @GetMapping("/custo-maquinas")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioCustoMaquinasResponseDTO> custoMaquinas(
            @RequestParam List<Long> maquinaIds,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano
    ) {
        List<RelatorioCustoMaquinaDTO> maquinas = calcularRelatorioCustoMaquinas(maquinaIds, mes, ano);

        BigDecimal totalGeral = maquinas.stream()
                .map(RelatorioCustoMaquinaDTO::custoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(new RelatorioCustoMaquinasResponseDTO(maquinas, totalGeral));
    }

    @GetMapping("/custo-maquinas/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoMaquinasPdf(
            @RequestParam List<Long> maquinaIds,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano
    ) throws DocumentException {
        List<RelatorioCustoMaquinaDTO> maquinas = calcularRelatorioCustoMaquinas(maquinaIds, mes, ano);

        BigDecimal totalGeral = maquinas.stream()
                .map(RelatorioCustoMaquinaDTO::custoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RelatorioCustoMaquinasResponseDTO relatorio = new RelatorioCustoMaquinasResponseDTO(maquinas, totalGeral);
        byte[] pdf = relatorioPdfBuilder.custoMaquinas(relatorio, mes, ano);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-custo-maquinas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/custo-setores")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioCustoSetoresResponseDTO> custoSetores(
            @RequestParam(required = false) List<Long> setorIds,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano
    ) {
        return ResponseEntity.ok(custoSetorService.calcularRelatorioCustoSetores(setorIds, mes, ano));
    }

    @GetMapping("/custo-setores/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoSetoresPdf(
            @RequestParam(required = false) List<Long> setorIds,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano
    ) throws DocumentException {
        RelatorioCustoSetoresResponseDTO relatorio = custoSetorService
                .calcularRelatorioCustoSetores(setorIds, mes, ano);

        byte[] pdf = relatorioPdfBuilder.custoSetores(relatorio, setorIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-custo-setores.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    private BigDecimal calcularValorRealizadoMensal(Integer mes, Integer ano) {
        BigDecimal valorPecas = solicitacaoCompraRepository.calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorPecas == null) valorPecas = BigDecimal.ZERO;

        BigDecimal valorServicoTerceiro = servicoTerceiroRepository.somarValorServicoTerceiroPorMesEAno(mes, ano);
        if (valorServicoTerceiro == null) valorServicoTerceiro = BigDecimal.ZERO;

        return valorPecas.add(valorServicoTerceiro);
    }

    private BigDecimal calcularValorRealizadoAnual(Integer ano) {
        BigDecimal valorPecas = solicitacaoCompraRepository.calcularGastoRealizadoPorAno(ano);
        if (valorPecas == null) valorPecas = BigDecimal.ZERO;

        BigDecimal valorServicoTerceiro = servicoTerceiroRepository.somarValorServicoTerceiroPorAno(ano);
        if (valorServicoTerceiro == null) valorServicoTerceiro = BigDecimal.ZERO;

        return valorPecas.add(valorServicoTerceiro);
    }

    private List<RelatorioCustoMaquinaDTO> calcularRelatorioCustoMaquinas(
            List<Long> maquinaIds,
            Integer mes,
            Integer ano
    ) {
        if (mes != null && ano == null) {
            throw new RelatorioParametrosInvalidosException("ano é obrigatório quando mes é informado.");
        }

        List<Maquina> maquinas = new ArrayList<>();
        for (Long maquinaId : maquinaIds) {
            maquinas.add(maquinaRepository.findById(maquinaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Máquina com ID " + maquinaId + " não encontrada."
                    )));
        }

        List<RelatorioCustoMaquinaDTO> resultado = new ArrayList<>();
        for (Maquina maquina : maquinas) {
            Long maquinaId = maquina.getId();
            BigDecimal custoPecas;
            BigDecimal custoMaoDeObra;
            Integer mesResultado = null;
            Integer anoResultado = null;

            if (mes != null && ano != null) {
                custoPecas = movimentacaoEstoqueRepository
                        .calcularCustoPecasPorMaquinaMesEAno(maquinaId, mes, ano);
                custoMaoDeObra = custoManutencaoService
                        .calcularCustoMaoDeObraPorMaquinaMesEAno(maquinaId, mes, ano);
                mesResultado = mes;
                anoResultado = ano;
            } else if (ano != null) {
                custoPecas = movimentacaoEstoqueRepository
                        .calcularCustoPecasPorMaquinaEAno(maquinaId, ano);
                custoMaoDeObra = custoManutencaoService
                        .calcularCustoMaoDeObraPorMaquinaEAno(maquinaId, ano);
                anoResultado = ano;
            } else {
                custoPecas = movimentacaoEstoqueRepository
                        .calcularCustoPecasPorMaquinaTotal(maquinaId);
                custoMaoDeObra = custoManutencaoService
                        .calcularCustoMaoDeObraPorMaquinaTotal(maquinaId);
            }

            if (custoPecas == null) custoPecas = BigDecimal.ZERO;
            if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

            resultado.add(new RelatorioCustoMaquinaDTO(
                    maquinaId,
                    maquina.getCodigo(),
                    mesResultado,
                    anoResultado,
                    custoPecas,
                    custoMaoDeObra,
                    custoPecas.add(custoMaoDeObra)
            ));
        }

        return resultado;
    }
}
