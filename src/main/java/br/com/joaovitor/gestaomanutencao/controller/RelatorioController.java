package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMensalDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinaDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioGastoRealizadoDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoAnualDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoMensalDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.repository.OrcamentoMensalRepository;
import br.com.joaovitor.gestaomanutencao.repository.SolicitacaoCompraRepository;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MaquinaRepository maquinaRepository;
    private final SolicitacaoCompraRepository solicitacaoCompraRepository;
    private final OrcamentoMensalRepository orcamentoMensalRepository;

    public RelatorioController(
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            ManutencaoRepository manutencaoRepository,
            MaquinaRepository maquinaRepository,
            SolicitacaoCompraRepository solicitacaoCompraRepository,
            OrcamentoMensalRepository orcamentoMensalRepository
    ) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.maquinaRepository = maquinaRepository;
        this.solicitacaoCompraRepository = solicitacaoCompraRepository;
        this.orcamentoMensalRepository = orcamentoMensalRepository;
    }

    @GetMapping("/custo-mensal")
    @Transactional(readOnly = true)
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

    @GetMapping("/custo-mensal/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> custoMensalPdf(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) throws DocumentException {
        BigDecimal custoPecas = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = manutencaoRepository.calcularCustoMaoDeObraPorMesEAno(mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        BigDecimal custoTotal = custoPecas.add(custoMaoDeObra);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Custo Mensal"));
        document.add(new Paragraph("Mês: " + mes));
        document.add(new Paragraph("Ano: " + ano));
        document.add(new Paragraph("Custo de Peças: " + custoPecas));
        document.add(new Paragraph("Custo de Mão de Obra: " + custoMaoDeObra));
        document.add(new Paragraph("Custo Total: " + custoTotal));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-custo-mensal.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
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
        BigDecimal valorRealizado = solicitacaoCompraRepository
                .calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorRealizado == null) valorRealizado = BigDecimal.ZERO;

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
        BigDecimal valorRealizado = solicitacaoCompraRepository
                .calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorRealizado == null) valorRealizado = BigDecimal.ZERO;

        BigDecimal saldoDisponivel = valorPlanejado.subtract(valorRealizado);
        BigDecimal percentualUtilizado = valorPlanejado.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : valorRealizado
                .divide(valorPlanejado, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Orçamento Mensal"));
        document.add(new Paragraph("Mês: " + mes));
        document.add(new Paragraph("Ano: " + ano));
        document.add(new Paragraph("Valor Planejado: " + valorPlanejado));
        document.add(new Paragraph("Valor Realizado: " + valorRealizado));
        document.add(new Paragraph("Saldo Disponível: " + saldoDisponivel));
        document.add(new Paragraph("Percentual Utilizado: " + percentualUtilizado));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-orcamento-mensal.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
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

        BigDecimal valorRealizadoTotal = solicitacaoCompraRepository.calcularGastoRealizadoPorAno(ano);
        if (valorRealizadoTotal == null) valorRealizadoTotal = BigDecimal.ZERO;

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

        BigDecimal valorRealizadoTotal = solicitacaoCompraRepository.calcularGastoRealizadoPorAno(ano);
        if (valorRealizadoTotal == null) valorRealizadoTotal = BigDecimal.ZERO;

        BigDecimal saldoDisponivel = valorPlanejadoTotal.subtract(valorRealizadoTotal);
        BigDecimal percentualUtilizado = valorPlanejadoTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : valorRealizadoTotal
                .divide(valorPlanejadoTotal, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Orçamento Anual"));
        document.add(new Paragraph("Ano: " + ano));
        document.add(new Paragraph("Valor Planejado: " + valorPlanejadoTotal));
        document.add(new Paragraph("Valor Realizado: " + valorRealizadoTotal));
        document.add(new Paragraph("Saldo Disponível: " + saldoDisponivel));
        document.add(new Paragraph("Percentual Utilizado: " + percentualUtilizado));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-orcamento-anual.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    @GetMapping("/gasto-realizado")
    @Transactional(readOnly = true)
    public ResponseEntity<RelatorioGastoRealizadoDTO> gastoRealizado(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        BigDecimal valorGasto = solicitacaoCompraRepository.calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorGasto == null) valorGasto = BigDecimal.ZERO;

        return ResponseEntity.ok(new RelatorioGastoRealizadoDTO(mes, ano, valorGasto));
    }

    @GetMapping("/gasto-realizado/pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> gastoRealizadoPdf(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) throws DocumentException {
        BigDecimal valorGasto = solicitacaoCompraRepository.calcularGastoRealizadoPorMesEAno(mes, ano);
        if (valorGasto == null) valorGasto = BigDecimal.ZERO;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Gasto Realizado"));
        document.add(new Paragraph("Mês: " + mes));
        document.add(new Paragraph("Ano: " + ano));
        document.add(new Paragraph("Valor Gasto: " + valorGasto));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=relatorio-gasto-realizado.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
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

        BigDecimal custoMaoDeObra = manutencaoRepository
                    .calcularCustoMaoDeObraPorMaquinaMesEAno(maquinaId, mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Custo por Máquina - Mensal"));
        document.add(new Paragraph("Código da Máquina: " + maquina.getCodigo()));
        document.add(new Paragraph("Mês: " + mes));
        document.add(new Paragraph("Ano: " + ano));
        document.add(new Paragraph("Custo de Peças: " + custoPecas));
        document.add(new Paragraph("Custo de Mão de Obra: " + custoMaoDeObra));
        document.add(new Paragraph("Custo Total: " + custoPecas.add(custoMaoDeObra)));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=relatorio-custo-maquina-mensal.pdf");

        return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
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

        BigDecimal custoMaoDeObra = manutencaoRepository
                    .calcularCustoMaoDeObraPorMaquinaEAno(maquinaId, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Custo por Máquina - Anual"));
        document.add(new Paragraph("Código da Máquina: " + maquina.getCodigo()));
        document.add(new Paragraph("Ano: " + ano));
        document.add(new Paragraph("Custo de Peças: " + custoPecas));
        document.add(new Paragraph("Custo de Mão de Obra: " + custoMaoDeObra));
        document.add(new Paragraph("Custo Total: " + custoPecas.add(custoMaoDeObra)));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=relatorio-custo-maquina-anual.pdf");

        return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
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

        BigDecimal custoMaoDeObra = manutencaoRepository
                    .calcularCustoMaoDeObraPorMaquinaTotal(maquinaId);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Relatório de Custo por Máquina - Total"));
        document.add(new Paragraph("Código da Máquina: " + maquina.getCodigo()));
        document.add(new Paragraph("Custo de Peças: " + custoPecas));
        document.add(new Paragraph("Custo de Mão de Obra: " + custoMaoDeObra));
        document.add(new Paragraph("Custo Total: " + custoPecas.add(custoMaoDeObra)));
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=relatorio-custo-maquina-total.pdf");

        return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
    }
}
