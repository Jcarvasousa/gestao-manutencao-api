package br.com.joaovitor.gestaomanutencao.pdf;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinaDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinasResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMensalDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetorDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetoresResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioGastoRealizadoDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoAnualDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoMensalDTO;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class RelatorioPdfBuilder {

    private static final String NOME_SISTEMA = "Gestão de Manutenção";
    private static final ZoneId FUSO_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR);
    private static final int MAXIMO_MAQUINAS_GRAFICO = 10;
    private static final String SEM_CUSTOS = "Sem custos no período";
    private static final String SEM_VALORES = "Sem valores no período";
    private static final String PREFIXO_ANO = "Ano: ";

    private static final Color COR_DESTAQUE = new Color(13, 148, 136);
    private static final Color COR_SECUNDARIA = new Color(100, 116, 139);
    private static final Color COR_LINHA_ALTERNADA = new Color(245, 247, 246);
    private static final Color COR_TEXTO = new Color(31, 41, 55);
    private static final Color COR_BORDA = new Color(203, 213, 225);
    private static final Color COR_TOTAL_FUNDO = new Color(226, 232, 240);
    private static final Color COR_RESUMO_FUNDO = new Color(240, 253, 250);

    // ---------------------------------------------------------------
    // Fontes (imobiliário local por documento - sem estado compartilhado)
    // ---------------------------------------------------------------

    private record Fontes(
            BaseFont baseFont,
            Font titulo,
            Font secao,
            Font normal,
            Font normalNegrito,
            Font italico,
            Font tabelaCabecalho,
            Font tabelaCelula,
            Font tabelaTotal,
            Font resumoValor
    ) {
    }

    private static Fontes criarFontes() {
        BaseFont baseFont;
        try {
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Não foi possível carregar a fonte do relatório.", e);
        }
        return new Fontes(
                baseFont,
                new Font(baseFont, 18, Font.BOLD, Color.WHITE),
                new Font(baseFont, 13, Font.BOLD, COR_TEXTO),
                new Font(baseFont, 10, Font.NORMAL, COR_TEXTO),
                new Font(baseFont, 10, Font.BOLD, COR_TEXTO),
                new Font(baseFont, 10, Font.ITALIC, COR_TEXTO),
                new Font(baseFont, 10, Font.BOLD, Color.WHITE),
                new Font(baseFont, 9, Font.NORMAL, COR_TEXTO),
                new Font(baseFont, 10, Font.BOLD, COR_TEXTO),
                new Font(baseFont, 20, Font.BOLD, COR_DESTAQUE)
        );
    }

    // ---------------------------------------------------------------
    // Relatórios
    // ---------------------------------------------------------------

    public byte[] custoMensal(RelatorioCustoMensalDTO dto) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento("Relatório de Custo Mensal");
        adicionarBlocoIdentificacao(pdf, FormatadorRelatorio.periodo(dto.mes(), dto.ano()), "Nenhum");
        adicionarResumo(pdf, "Custo total do período", dto.custoTotal(), List.of());
        adicionarComoLer(pdf, List.of(
                "Este relatório considera apenas manutenções concluídas no mês e ano informados, com base na data de conclusão.",
                "O custo de peças é calculado pelas saídas de estoque usadas nas manutenções, descontando as devoluções, pelo custo unitário registrado no momento da saída.",
                "A mão de obra (técnicos + terceiros) soma as horas dos técnicos internos multiplicadas pelo custo da hora de cada um, mais os serviços de terceiros (valor final quando informado, ou valor apurado caso contrário).",
                "Manutenções em andamento ou canceladas não entram nestes cálculos."
        ));

        adicionarGraficoEmpilhado(pdf, "Peças", dto.custoPecas(), "Mão de obra (técnicos + terceiros)", dto.custoMaoDeObra(), SEM_CUSTOS);

        adicionarTabelaItemValor(
                pdf,
                "Valor",
                List.of(
                        linha("Custo de Peças", dto.custoPecas()),
                        linha("Mão de obra (técnicos + terceiros)", dto.custoMaoDeObra())
                ),
                "Custo Total",
                dto.custoTotal()
        );

        return fecharDocumento(pdf);
    }

    public byte[] orcamentoMensal(RelatorioOrcamentoMensalDTO dto) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento("Relatório de Orçamento Mensal");
        adicionarBlocoIdentificacao(pdf, FormatadorRelatorio.periodo(dto.mes(), dto.ano()), "Nenhum");
        adicionarResumo(pdf, "Valor realizado no período", dto.valorRealizado(), List.of(
                linhaFormatada("Valor planejado", FormatadorRelatorio.moeda(dto.valorPlanejado())),
                linhaFormatada("Percentual utilizado", FormatadorRelatorio.percentual(dto.percentualUtilizado()))
        ));
        adicionarComoLer(pdf, List.of(
                "Este relatório compara o valor planejado no orçamento do mês com o valor efetivamente realizado no mesmo período.",
                "O valor realizado soma as compras de peças recebidas no período (pelo valor pago) mais os serviços de terceiros de manutenções concluídas no período; salário de técnico não entra nesse total.",
                "Esse critério é diferente do custo de manutenção, que mede o consumo de peças do estoque e a mão de obra das manutenções concluídas, e não o que foi efetivamente pago no período.",
                "O saldo disponível é o valor planejado menos o valor realizado, podendo ficar negativo se o realizado ultrapassar o planejado."
        ));

        adicionarGraficoComparativo(pdf, "Planejado", dto.valorPlanejado(), "Realizado", dto.valorRealizado(), dto.percentualUtilizado());

        adicionarTabelaOrcamento(pdf, dto.valorPlanejado(), dto.valorRealizado(),
                dto.saldoDisponivel(), dto.percentualUtilizado());

        return fecharDocumento(pdf);
    }

    public byte[] orcamentoAnual(RelatorioOrcamentoAnualDTO dto) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento("Relatório de Orçamento Anual");
        adicionarBlocoIdentificacao(pdf, FormatadorRelatorio.periodo(null, dto.ano()), "Nenhum");
        adicionarResumo(pdf, "Valor realizado no período", dto.valorRealizadoTotal(), List.of(
                linhaFormatada("Valor planejado", FormatadorRelatorio.moeda(dto.valorPlanejadoTotal())),
                linhaFormatada("Percentual utilizado", FormatadorRelatorio.percentual(dto.percentualUtilizado()))
        ));
        adicionarComoLer(pdf, List.of(
                "Este relatório compara o valor planejado no orçamento do ano com o valor efetivamente realizado no mesmo período.",
                "O valor realizado soma as compras de peças recebidas no ano (pelo valor pago) mais os serviços de terceiros de manutenções concluídas no ano; salário de técnico não entra nesse total.",
                "Esse critério é diferente do custo de manutenção, que mede o consumo de peças do estoque e a mão de obra das manutenções concluídas, e não o que foi efetivamente pago no período.",
                "O saldo disponível é o valor planejado menos o valor realizado, podendo ficar negativo se o realizado ultrapassar o planejado."
        ));

        adicionarGraficoComparativo(pdf, "Planejado", dto.valorPlanejadoTotal(), "Realizado", dto.valorRealizadoTotal(), dto.percentualUtilizado());

        adicionarTabelaOrcamento(pdf, dto.valorPlanejadoTotal(), dto.valorRealizadoTotal(),
                dto.saldoDisponivel(), dto.percentualUtilizado());

        return fecharDocumento(pdf);
    }

    public byte[] gastoRealizado(RelatorioGastoRealizadoDTO dto) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento("Relatório de Gasto Realizado");
        adicionarBlocoIdentificacao(pdf, FormatadorRelatorio.periodo(dto.mes(), dto.ano()), "Nenhum");
        adicionarResumo(pdf, "Gasto total do período", dto.valorGastoTotal(), List.of());
        adicionarComoLer(pdf, List.of(
                "Este relatório soma os gastos efetivamente realizados no período: compras de peças recebidas (valor pago) e serviços de terceiros de manutenções concluídas.",
                "Salário de técnico interno não entra nesse total, pois não representa um gasto pontual pago no período.",
                "Esse valor é diferente do custo de manutenção, que mede o consumo de peças do estoque (saídas menos devoluções) e a mão de obra das manutenções concluídas, independentemente de quando a compra foi paga."
        ));

        adicionarGraficoEmpilhado(pdf, "Peças compradas", dto.valorGastoPecas(), "Serviços de terceiros", dto.valorGastoServicoTerceiro(), SEM_CUSTOS);

        adicionarTabelaItemValor(
                pdf,
                "Valor",
                List.of(
                        linha("Peças Compradas", dto.valorGastoPecas()),
                        linha("Serviços de Terceiros", dto.valorGastoServicoTerceiro())
                ),
                "Gasto Total",
                dto.valorGastoTotal()
        );

        return fecharDocumento(pdf);
    }

    public byte[] custoMaquinaMensal(RelatorioCustoMaquinaDTO dto) throws DocumentException {
        return custoMaquinaUnica(dto, "Relatório de Custo por Máquina - Mensal");
    }

    public byte[] custoMaquinaAnual(RelatorioCustoMaquinaDTO dto) throws DocumentException {
        return custoMaquinaUnica(dto, "Relatório de Custo por Máquina - Anual");
    }

    public byte[] custoMaquinaTotal(RelatorioCustoMaquinaDTO dto) throws DocumentException {
        return custoMaquinaUnica(dto, "Relatório de Custo por Máquina - Total");
    }

    private byte[] custoMaquinaUnica(RelatorioCustoMaquinaDTO dto, String titulo) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento(titulo);
        adicionarBlocoIdentificacao(
                pdf,
                FormatadorRelatorio.periodo(dto.mes(), dto.ano()),
                "Máquina: " + dto.maquinaCodigo()
        );
        adicionarResumo(pdf, "Custo total do período", dto.custoTotal(), List.of());
        adicionarComoLer(pdf, List.of(
                "Este relatório considera apenas manutenções concluídas dessa máquina no período informado, com base na data de conclusão.",
                "O custo de peças é calculado pelas saídas de estoque usadas nas manutenções da máquina, descontando as devoluções, pelo custo unitário registrado no momento da saída.",
                "A mão de obra (técnicos + terceiros) soma as horas dos técnicos internos multiplicadas pelo custo da hora de cada um, mais os serviços de terceiros (valor final quando informado, ou valor apurado caso contrário).",
                "Manutenções em andamento ou canceladas dessa máquina não entram nestes cálculos."
        ));

        adicionarTabelaItemValor(
                pdf,
                "Valor",
                List.of(
                        linha("Custo de Peças", dto.custoPecas()),
                        linha("Mão de obra (técnicos + terceiros)", dto.custoMaoDeObra())
                ),
                "Custo Total",
                dto.custoTotal()
        );

        return fecharDocumento(pdf);
    }

    public byte[] custoMaquinas(RelatorioCustoMaquinasResponseDTO dto, Integer mes, Integer ano) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento("Relatório de Custo por Máquinas");

        String filtros = dto.maquinas().isEmpty()
                ? "Nenhuma máquina informada"
                : "Máquinas: " + String.join(", ", dto.maquinas().stream().map(RelatorioCustoMaquinaDTO::maquinaCodigo).toList());

        adicionarBlocoIdentificacao(pdf, FormatadorRelatorio.periodo(mes, ano), filtros);
        adicionarResumo(pdf, "Custo total do período", dto.totalGeral(), List.of());
        adicionarComoLer(pdf, List.of(
                "Este relatório soma o custo de manutenção das máquinas selecionadas, considerando apenas manutenções concluídas no período informado.",
                "O custo de peças é calculado pelas saídas de estoque menos as devoluções, pelo custo unitário do momento da saída; a mão de obra soma horas de técnicos mais serviços de terceiros.",
                "Manutenções em andamento ou canceladas não entram nestes cálculos.",
                "O total geral é a soma do custo total de cada máquina exibida na tabela."
        ));

        List<RelatorioCustoMaquinaDTO> ordenadasPorCusto = dto.maquinas().stream()
                .sorted(Comparator.comparing(RelatorioCustoMaquinaDTO::custoTotal, Comparator.reverseOrder()))
                .toList();
        List<ItemBarra> itensGrafico = ordenadasPorCusto.stream()
                .limit(MAXIMO_MAQUINAS_GRAFICO)
                .map(m -> new ItemBarra(m.maquinaCodigo(), m.custoTotal()))
                .toList();
        String notaGrafico = ordenadasPorCusto.size() > MAXIMO_MAQUINAS_GRAFICO
                ? "Exibindo as " + MAXIMO_MAQUINAS_GRAFICO + " maiores de " + ordenadasPorCusto.size() + " máquinas."
                : null;
        adicionarGraficoBarrasMultiplas(pdf, itensGrafico, notaGrafico);

        BigDecimal totalPecas = BigDecimal.ZERO;
        BigDecimal totalMaoDeObra = BigDecimal.ZERO;
        List<PdfPCell[]> linhas = new ArrayList<>();
        for (RelatorioCustoMaquinaDTO maquina : dto.maquinas()) {
            BigDecimal pecas = maquina.custoPecas() == null ? BigDecimal.ZERO : maquina.custoPecas();
            BigDecimal maoDeObra = maquina.custoMaoDeObra() == null ? BigDecimal.ZERO : maquina.custoMaoDeObra();
            totalPecas = totalPecas.add(pecas);
            totalMaoDeObra = totalMaoDeObra.add(maoDeObra);
            linhas.add(new PdfPCell[]{
                    celulaTexto(pdf.fontes(), maquina.maquinaCodigo(), Element.ALIGN_LEFT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(maquina.custoPecas()), Element.ALIGN_RIGHT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(maquina.custoMaoDeObra()), Element.ALIGN_RIGHT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(maquina.custoTotal()), Element.ALIGN_RIGHT)
            });
        }
        adicionarTabelaGenerica(
                pdf,
                new String[]{"Código da Máquina", "Custo de Peças", "Mão de obra (técnicos + terceiros)", "Custo Total"},
                new int[]{Element.ALIGN_LEFT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT},
                new float[]{2.5f, 1.7f, 2.2f, 1.7f},
                linhas,
                new PdfPCell[]{
                        celulaTotal(pdf.fontes(), "Total Geral", Element.ALIGN_LEFT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(totalPecas), Element.ALIGN_RIGHT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(totalMaoDeObra), Element.ALIGN_RIGHT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(dto.totalGeral()), Element.ALIGN_RIGHT)
                }
        );

        return fecharDocumento(pdf);
    }

    public byte[] custoSetores(RelatorioCustoSetoresResponseDTO dto, List<Long> setorIdsFiltro) throws DocumentException {
        DocumentoPdf pdf = abrirDocumento("Relatório de Custo por Setores");

        String filtros = (setorIdsFiltro == null || setorIdsFiltro.isEmpty())
                ? "Nenhum (todos os setores)"
                : "Setores: " + String.join(", ", dto.setores().stream()
                .filter(s -> s.setorId() != null)
                .map(RelatorioCustoSetorDTO::setorNome)
                .toList());

        adicionarBlocoIdentificacao(pdf, FormatadorRelatorio.periodo(dto.mes(), dto.ano()), filtros);
        adicionarResumo(pdf, "Custo total do período", dto.totalGeral(), List.of());
        adicionarComoLer(pdf, List.of(
                "Este relatório mostra o custo de manutenção por setor, somando os custos das máquinas de cada setor a partir de manutenções concluídas no período informado.",
                "O custo de peças considera saídas de estoque menos devoluções; a mão de obra (técnicos + terceiros) soma horas de técnicos mais serviços de terceiros.",
                "O percentual do total é calculado sobre a soma dos setores exibidos neste relatório, não sobre a empresa inteira; por isso a soma pode não fechar exatamente 100% por arredondamento.",
                "Máquinas sem setor cadastrado aparecem agrupadas como \"Sem setor\" quando nenhum filtro de setor é aplicado."
        ));

        List<ItemBarra> itensGrafico = dto.setores().stream()
                .map(s -> new ItemBarra(s.setorNome(), s.custoTotal()))
                .toList();
        adicionarGraficoBarrasMultiplas(pdf, itensGrafico, null);

        BigDecimal totalPecas = BigDecimal.ZERO;
        BigDecimal totalMaoDeObra = BigDecimal.ZERO;
        int totalMaquinas = 0;
        List<PdfPCell[]> linhas = new ArrayList<>();
        for (RelatorioCustoSetorDTO setor : dto.setores()) {
            BigDecimal pecas = setor.custoPecas() == null ? BigDecimal.ZERO : setor.custoPecas();
            BigDecimal maoDeObra = setor.custoMaoDeObra() == null ? BigDecimal.ZERO : setor.custoMaoDeObra();
            totalPecas = totalPecas.add(pecas);
            totalMaoDeObra = totalMaoDeObra.add(maoDeObra);
            totalMaquinas += setor.quantidadeMaquinas() == null ? 0 : setor.quantidadeMaquinas();
            linhas.add(new PdfPCell[]{
                    celulaTexto(pdf.fontes(), setor.setorNome(), Element.ALIGN_LEFT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.simNao(setor.ativo()), Element.ALIGN_CENTER),
                    celulaTexto(pdf.fontes(), String.valueOf(setor.quantidadeMaquinas()), Element.ALIGN_CENTER),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(setor.custoPecas()), Element.ALIGN_RIGHT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(setor.custoMaoDeObra()), Element.ALIGN_RIGHT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(setor.custoTotal()), Element.ALIGN_RIGHT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.percentual(setor.percentualDoTotal()), Element.ALIGN_RIGHT)
            });
        }
        adicionarTabelaGenerica(
                pdf,
                new String[]{"Setor", "Ativo", "Máquinas", "Peças", "Mão de obra", "Total", "% total"},
                new int[]{Element.ALIGN_LEFT, Element.ALIGN_CENTER, Element.ALIGN_CENTER, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT},
                new float[]{2.6f, 0.9f, 1.1f, 1.5f, 1.5f, 1.5f, 1.1f},
                linhas,
                new PdfPCell[]{
                        celulaTotal(pdf.fontes(), "Total Geral", Element.ALIGN_LEFT),
                        celulaTotal(pdf.fontes(), "", Element.ALIGN_CENTER),
                        celulaTotal(pdf.fontes(), String.valueOf(totalMaquinas), Element.ALIGN_CENTER),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(totalPecas), Element.ALIGN_RIGHT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(totalMaoDeObra), Element.ALIGN_RIGHT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(dto.totalGeral()), Element.ALIGN_RIGHT),
                        celulaTotal(pdf.fontes(), "", Element.ALIGN_RIGHT)
                }
        );

        return fecharDocumento(pdf);
    }

    // ---------------------------------------------------------------
    // Estrutura comum do documento
    // ---------------------------------------------------------------

    private record DocumentoPdf(Document document, PdfWriter writer, ByteArrayOutputStream saida, Fontes fontes,
                                 ZonedDateTime dataGeracao) {
    }

    private record ItemBarra(String rotulo, BigDecimal valor) {
    }

    private DocumentoPdf abrirDocumento(String titulo) throws DocumentException {
        Fontes fontes = criarFontes();
        ZonedDateTime dataGeracao = ZonedDateTime.now(FUSO_SAO_PAULO);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, saida);
        writer.setPageEvent(new RodapePageEvent(NOME_SISTEMA, dataGeracao));
        document.open();
        adicionarFaixaCabecalho(document, fontes, titulo);
        return new DocumentoPdf(document, writer, saida, fontes, dataGeracao);
    }

    private byte[] fecharDocumento(DocumentoPdf pdf) {
        pdf.document().close();
        return pdf.saida().toByteArray();
    }

    private void adicionarFaixaCabecalho(Document document, Fontes fontes, String titulo) throws DocumentException {
        PdfPTable faixa = new PdfPTable(1);
        faixa.setWidthPercentage(100);
        PdfPCell celula = new PdfPCell(new Phrase(titulo, fontes.titulo()));
        celula.setBackgroundColor(COR_DESTAQUE);
        celula.setBorder(0);
        celula.setPadding(14);
        celula.setHorizontalAlignment(Element.ALIGN_LEFT);
        faixa.addCell(celula);
        document.add(faixa);
        document.add(espacador(fontes));
    }

    private void adicionarBlocoIdentificacao(DocumentoPdf pdf, String periodoDescricao, String filtrosDescricao)
            throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        document.add(paragrafoRotuloValor(fontes, "Período: ", removerPrefixoAno(periodoDescricao)));
        document.add(paragrafoRotuloValor(fontes, "Filtros aplicados: ", filtrosDescricao));
        String dataGeracao = FORMATO_DATA_HORA.format(pdf.dataGeracao());
        document.add(paragrafoRotuloValor(fontes, "Gerado em: ", dataGeracao + " (horário de Brasília)"));
        document.add(espacador(fontes));
    }

    private String removerPrefixoAno(String periodoDescricao) {
        return periodoDescricao.startsWith(PREFIXO_ANO)
                ? periodoDescricao.substring(PREFIXO_ANO.length())
                : periodoDescricao;
    }

    private void adicionarResumo(DocumentoPdf pdf, String rotuloPrincipal, BigDecimal valorPrincipal,
                                  List<String[]> linhasExtras) throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        document.add(tituloSecao(fontes, "Resumo"));

        PdfPTable caixa = new PdfPTable(1);
        caixa.setWidthPercentage(100);
        PdfPCell celula = new PdfPCell();
        celula.setBackgroundColor(COR_RESUMO_FUNDO);
        celula.setBorderColor(COR_DESTAQUE);
        celula.setBorderWidth(1);
        celula.setPadding(10);

        Paragraph principal = new Paragraph();
        principal.add(new Chunk(rotuloPrincipal + ": ", fontes.normalNegrito()));
        principal.add(new Chunk(FormatadorRelatorio.moeda(valorPrincipal), fontes.resumoValor()));
        celula.addElement(principal);

        for (String[] linha : linhasExtras) {
            Paragraph extra = new Paragraph();
            extra.add(new Chunk(linha[0] + ": ", fontes.normalNegrito()));
            extra.add(new Chunk(linha[1], fontes.normal()));
            extra.setSpacingBefore(3);
            celula.addElement(extra);
        }

        caixa.addCell(celula);
        document.add(caixa);
        document.add(espacador(fontes));
    }

    private void adicionarComoLer(DocumentoPdf pdf, List<String> frases) throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        document.add(tituloSecao(fontes, "Como ler este relatório"));
        for (String frase : frases) {
            Paragraph p = new Paragraph("• " + frase, fontes.normal());
            p.setSpacingAfter(2);
            document.add(p);
        }
        document.add(espacador(fontes));
    }

    private Paragraph tituloSecao(Fontes fontes, String texto) {
        Paragraph p = new Paragraph(texto, fontes.secao());
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph paragrafoRotuloValor(Fontes fontes, String rotulo, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(rotulo, fontes.normalNegrito()));
        p.add(new Chunk(valor, fontes.normal()));
        p.setSpacingAfter(2);
        return p;
    }

    private Paragraph espacador(Fontes fontes) {
        Paragraph p = new Paragraph(" ", new Font(fontes.baseFont(), 4));
        p.setLeading(7f);
        return p;
    }

    // ---------------------------------------------------------------
    // Tabelas
    // ---------------------------------------------------------------

    private void adicionarTabelaItemValor(DocumentoPdf pdf, String rotuloColunaValor,
                                           List<String[]> itens, String rotuloTotal, BigDecimal total)
            throws DocumentException {
        List<PdfPCell[]> linhas = new ArrayList<>();
        for (String[] item : itens) {
            linhas.add(new PdfPCell[]{
                    celulaTexto(pdf.fontes(), item[0], Element.ALIGN_LEFT),
                    celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(new BigDecimal(item[1])), Element.ALIGN_RIGHT)
            });
        }
        adicionarTabelaGenerica(
                pdf,
                new String[]{"Item", rotuloColunaValor},
                new int[]{Element.ALIGN_LEFT, Element.ALIGN_RIGHT},
                new float[]{3f, 2f},
                linhas,
                new PdfPCell[]{
                        celulaTotal(pdf.fontes(), rotuloTotal, Element.ALIGN_LEFT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.moeda(total), Element.ALIGN_RIGHT)
                }
        );
    }

    private void adicionarTabelaOrcamento(DocumentoPdf pdf, BigDecimal planejado, BigDecimal realizado,
                                           BigDecimal saldo, BigDecimal percentual) throws DocumentException {
        List<PdfPCell[]> linhas = new ArrayList<>();
        linhas.add(new PdfPCell[]{
                celulaTexto(pdf.fontes(), "Valor Planejado", Element.ALIGN_LEFT),
                celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(planejado), Element.ALIGN_RIGHT)
        });
        linhas.add(new PdfPCell[]{
                celulaTexto(pdf.fontes(), "Valor Realizado", Element.ALIGN_LEFT),
                celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(realizado), Element.ALIGN_RIGHT)
        });
        linhas.add(new PdfPCell[]{
                celulaTexto(pdf.fontes(), "Saldo Disponível", Element.ALIGN_LEFT),
                celulaTexto(pdf.fontes(), FormatadorRelatorio.moeda(saldo), Element.ALIGN_RIGHT)
        });
        adicionarTabelaGenerica(
                pdf,
                new String[]{"Item", "Valor"},
                new int[]{Element.ALIGN_LEFT, Element.ALIGN_RIGHT},
                new float[]{3f, 2f},
                linhas,
                new PdfPCell[]{
                        celulaTotal(pdf.fontes(), "Percentual Utilizado", Element.ALIGN_LEFT),
                        celulaTotal(pdf.fontes(), FormatadorRelatorio.percentual(percentual), Element.ALIGN_RIGHT)
                }
        );
    }

    private void adicionarTabelaGenerica(DocumentoPdf pdf, String[] cabecalhos, int[] alinhamentos,
                                          float[] larguras, List<PdfPCell[]> linhas, PdfPCell[] linhaTotal)
            throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        document.add(tituloSecao(fontes, "Dados do Relatório"));

        if (linhas.isEmpty()) {
            document.add(new Paragraph("Sem dados para os parâmetros informados.", fontes.italico()));
            document.add(espacador(fontes));
            return;
        }

        PdfPTable tabela = new PdfPTable(cabecalhos.length);
        tabela.setWidthPercentage(100);
        tabela.setWidths(larguras);
        tabela.setHeaderRows(1);

        for (int i = 0; i < cabecalhos.length; i++) {
            tabela.addCell(celulaCabecalho(fontes, cabecalhos[i], alinhamentos[i]));
        }

        boolean linhaPar = false;
        for (PdfPCell[] linha : linhas) {
            for (PdfPCell celula : linha) {
                if (linhaPar) {
                    celula.setBackgroundColor(COR_LINHA_ALTERNADA);
                }
                tabela.addCell(celula);
            }
            linhaPar = !linhaPar;
        }

        for (PdfPCell celula : linhaTotal) {
            tabela.addCell(celula);
        }

        document.add(tabela);
        document.add(espacador(fontes));
    }

    private PdfPCell celulaCabecalho(Fontes fontes, String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontes.tabelaCabecalho()));
        cell.setBackgroundColor(COR_DESTAQUE);
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(6);
        cell.setBorderColor(COR_DESTAQUE);
        return cell;
    }

    private PdfPCell celulaTexto(Fontes fontes, String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontes.tabelaCelula()));
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(5);
        cell.setBorderColor(COR_BORDA);
        return cell;
    }

    private PdfPCell celulaTotal(Fontes fontes, String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontes.tabelaTotal()));
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(6);
        cell.setBorderColor(COR_BORDA);
        cell.setBackgroundColor(COR_TOTAL_FUNDO);
        return cell;
    }

    // ---------------------------------------------------------------
    // Gráficos
    // ---------------------------------------------------------------

    private void adicionarGraficoBarrasMultiplas(DocumentoPdf pdf, List<ItemBarra> itens, String notaRodape)
            throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        BaseFont baseFont = fontes.baseFont();

        BigDecimal maior = itens.stream()
                .map(ItemBarra::valor)
                .filter(v -> v != null)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        if (itens.isEmpty() || maior.compareTo(BigDecimal.ZERO) <= 0) {
            document.add(tituloSecao(fontes, "Gráfico"));
            document.add(new Paragraph(SEM_CUSTOS, fontes.italico()));
            document.add(espacador(fontes));
            return;
        }

        float largura = document.right() - document.left();
        float alturaBarra = 16f;
        float espacamento = 10f;
        float margemRotulo = 150f;
        float margemDireita = 90f;
        float larguraBarraMax = largura - margemRotulo - margemDireita;
        float alturaTotal = itens.size() * (alturaBarra + espacamento) + 10f;

        PdfContentByte conteudo = pdf.writer().getDirectContent();
        PdfTemplate template = conteudo.createTemplate(largura, alturaTotal);

        float y = alturaTotal - 5f;
        for (ItemBarra item : itens) {
            BigDecimal valor = item.valor() == null ? BigDecimal.ZERO : item.valor();
            float proporcao = valor.floatValue() / maior.floatValue();
            float larguraBarra = valor.compareTo(BigDecimal.ZERO) <= 0 ? 0f : Math.max(2f, larguraBarraMax * proporcao);
            float meioBarraY = y - alturaBarra / 2f - 3f;

            template.beginText();
            template.setFontAndSize(baseFont, 8);
            template.setColorFill(COR_TEXTO);
            template.showTextAligned(PdfContentByte.ALIGN_RIGHT, truncar(item.rotulo(), 26), margemRotulo - 6, meioBarraY, 0);
            template.endText();

            if (larguraBarra > 0) {
                template.setColorFill(COR_DESTAQUE);
                template.rectangle(margemRotulo, y - alturaBarra, larguraBarra, alturaBarra);
                template.fill();
            }

            template.beginText();
            template.setFontAndSize(baseFont, 8);
            template.setColorFill(COR_TEXTO);
            template.showTextAligned(PdfContentByte.ALIGN_LEFT, FormatadorRelatorio.moeda(valor),
                    margemRotulo + larguraBarra + 6, meioBarraY, 0);
            template.endText();

            y -= (alturaBarra + espacamento);
        }

        Image imagem = Image.getInstance(template);
        adicionarBlocoGrafico(pdf, imagem, notaRodape);
    }

    private void adicionarGraficoEmpilhado(DocumentoPdf pdf, String rotulo1, BigDecimal valor1,
                                            String rotulo2, BigDecimal valor2, String mensagemVazio)
            throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        BaseFont baseFont = fontes.baseFont();

        BigDecimal v1 = valor1 == null ? BigDecimal.ZERO : valor1;
        BigDecimal v2 = valor2 == null ? BigDecimal.ZERO : valor2;
        BigDecimal total = v1.add(v2);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            document.add(tituloSecao(fontes, "Gráfico"));
            document.add(new Paragraph(mensagemVazio, fontes.italico()));
            document.add(espacador(fontes));
            return;
        }

        float largura = document.right() - document.left();
        float alturaBarra = 30f;
        float alturaTotal = alturaBarra + 38f;

        PdfContentByte conteudo = pdf.writer().getDirectContent();
        PdfTemplate template = conteudo.createTemplate(largura, alturaTotal);

        float proporcao1 = v1.floatValue() / total.floatValue();
        float largura1 = largura * proporcao1;
        float largura2 = largura - largura1;
        float yBarra = alturaTotal - alturaBarra;

        if (largura1 > 0) {
            template.setColorFill(COR_DESTAQUE);
            template.rectangle(0, yBarra, largura1, alturaBarra);
            template.fill();
        }
        if (largura2 > 0) {
            template.setColorFill(COR_SECUNDARIA);
            template.rectangle(largura1, yBarra, largura2, alturaBarra);
            template.fill();
        }

        if (largura1 >= 60) {
            template.beginText();
            template.setFontAndSize(baseFont, 9);
            template.setColorFill(Color.WHITE);
            template.showTextAligned(PdfContentByte.ALIGN_CENTER, FormatadorRelatorio.moeda(v1),
                    largura1 / 2, yBarra + alturaBarra / 2 - 3, 0);
            template.endText();
        }
        if (largura2 >= 60) {
            template.beginText();
            template.setFontAndSize(baseFont, 9);
            template.setColorFill(Color.WHITE);
            template.showTextAligned(PdfContentByte.ALIGN_CENTER, FormatadorRelatorio.moeda(v2),
                    largura1 + largura2 / 2, yBarra + alturaBarra / 2 - 3, 0);
            template.endText();
        }

        float yLegenda = yBarra - 18f;
        String texto1 = rotulo1 + ": " + FormatadorRelatorio.moeda(v1);
        template.setColorFill(COR_DESTAQUE);
        template.rectangle(0, yLegenda, 10, 10);
        template.fill();
        template.beginText();
        template.setFontAndSize(baseFont, 8);
        template.setColorFill(COR_TEXTO);
        template.showTextAligned(PdfContentByte.ALIGN_LEFT, texto1, 16, yLegenda + 1, 0);
        template.endText();

        float xLegenda2 = 16 + baseFont.getWidthPoint(texto1, 8) + 30;
        String texto2 = rotulo2 + ": " + FormatadorRelatorio.moeda(v2);
        template.setColorFill(COR_SECUNDARIA);
        template.rectangle(xLegenda2, yLegenda, 10, 10);
        template.fill();
        template.beginText();
        template.setFontAndSize(baseFont, 8);
        template.setColorFill(COR_TEXTO);
        template.showTextAligned(PdfContentByte.ALIGN_LEFT, texto2, xLegenda2 + 16, yLegenda + 1, 0);
        template.endText();

        Image imagem = Image.getInstance(template);
        adicionarBlocoGrafico(pdf, imagem, null);
    }

    private void adicionarGraficoComparativo(DocumentoPdf pdf, String rotulo1, BigDecimal valor1,
                                              String rotulo2, BigDecimal valor2, BigDecimal percentualUtilizado)
            throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();
        BaseFont baseFont = fontes.baseFont();

        BigDecimal v1 = valor1 == null ? BigDecimal.ZERO : valor1;
        BigDecimal v2 = valor2 == null ? BigDecimal.ZERO : valor2;
        BigDecimal maior = v1.max(v2);

        if (maior.compareTo(BigDecimal.ZERO) <= 0) {
            document.add(tituloSecao(fontes, "Gráfico"));
            document.add(new Paragraph(SEM_VALORES, fontes.italico()));
            document.add(espacador(fontes));
            return;
        }

        float largura = document.right() - document.left();
        float alturaBarra = 18f;
        float espacamento = 14f;
        float margemRotulo = 90f;
        float margemDireita = 90f;
        float larguraBarraMax = largura - margemRotulo - margemDireita;
        float alturaTotal = 2 * alturaBarra + espacamento + 34f;

        PdfContentByte conteudo = pdf.writer().getDirectContent();
        PdfTemplate template = conteudo.createTemplate(largura, alturaTotal);

        float y = alturaTotal - 8f;
        y = desenharLinhaBarraComparativa(baseFont, template, rotulo1, v1, maior, margemRotulo, larguraBarraMax, y, alturaBarra, COR_DESTAQUE);
        y -= espacamento;
        y = desenharLinhaBarraComparativa(baseFont, template, rotulo2, v2, maior, margemRotulo, larguraBarraMax, y, alturaBarra, COR_SECUNDARIA);

        template.beginText();
        template.setFontAndSize(baseFont, 9);
        template.setColorFill(COR_TEXTO);
        template.showTextAligned(PdfContentByte.ALIGN_LEFT,
                "Percentual utilizado: " + FormatadorRelatorio.percentual(percentualUtilizado),
                margemRotulo, y - 16, 0);
        template.endText();

        Image imagem = Image.getInstance(template);
        adicionarBlocoGrafico(pdf, imagem, null);
    }

    private float desenharLinhaBarraComparativa(BaseFont baseFont, PdfTemplate template, String rotulo, BigDecimal valor, BigDecimal maior,
                                                 float margemRotulo, float larguraBarraMax, float y,
                                                 float alturaBarra, Color cor) {
        float proporcao = valor.floatValue() / maior.floatValue();
        float larguraBarra = valor.compareTo(BigDecimal.ZERO) <= 0 ? 0f : Math.max(2f, larguraBarraMax * proporcao);
        float meioBarraY = y - alturaBarra / 2f - 3f;

        template.beginText();
        template.setFontAndSize(baseFont, 8);
        template.setColorFill(COR_TEXTO);
        template.showTextAligned(PdfContentByte.ALIGN_RIGHT, rotulo, margemRotulo - 6, meioBarraY, 0);
        template.endText();

        if (larguraBarra > 0) {
            template.setColorFill(cor);
            template.rectangle(margemRotulo, y - alturaBarra, larguraBarra, alturaBarra);
            template.fill();
        }

        template.beginText();
        template.setFontAndSize(baseFont, 8);
        template.setColorFill(COR_TEXTO);
        template.showTextAligned(PdfContentByte.ALIGN_LEFT, FormatadorRelatorio.moeda(valor),
                margemRotulo + larguraBarra + 6, meioBarraY, 0);
        template.endText();

        return y - alturaBarra;
    }

    /**
     * Agrupa o título "Gráfico" e a imagem em um único bloco (keepTogether) para que nunca
     * sejam quebrados entre páginas.
     */
    private void adicionarBlocoGrafico(DocumentoPdf pdf, Image imagem, String notaRodape) throws DocumentException {
        Document document = pdf.document();
        Fontes fontes = pdf.fontes();

        PdfPTable bloco = new PdfPTable(1);
        bloco.setWidthPercentage(100);
        bloco.setKeepTogether(true);

        PdfPCell celulaTitulo = new PdfPCell();
        celulaTitulo.setBorder(0);
        celulaTitulo.setPadding(0);
        celulaTitulo.setPaddingBottom(4);
        celulaTitulo.addElement(new Paragraph("Gráfico", fontes.secao()));
        bloco.addCell(celulaTitulo);

        PdfPCell celulaImagem = new PdfPCell();
        celulaImagem.setBorder(0);
        celulaImagem.setPadding(0);
        celulaImagem.addElement(imagem);
        bloco.addCell(celulaImagem);

        if (notaRodape != null) {
            PdfPCell celulaNota = new PdfPCell();
            celulaNota.setBorder(0);
            celulaNota.setPadding(0);
            celulaNota.setPaddingTop(4);
            celulaNota.addElement(new Paragraph(notaRodape, fontes.italico()));
            bloco.addCell(celulaNota);
        }

        document.add(bloco);
        document.add(espacador(fontes));
    }

    private String truncar(String texto, int maxCaracteres) {
        if (texto == null) return "";
        return texto.length() <= maxCaracteres ? texto : texto.substring(0, maxCaracteres - 1) + "…";
    }

    private String[] linha(String rotulo, BigDecimal valor) {
        return new String[]{rotulo, (valor == null ? BigDecimal.ZERO : valor).toPlainString()};
    }

    private String[] linhaFormatada(String rotulo, String valorFormatado) {
        return new String[]{rotulo, valorFormatado};
    }
}
