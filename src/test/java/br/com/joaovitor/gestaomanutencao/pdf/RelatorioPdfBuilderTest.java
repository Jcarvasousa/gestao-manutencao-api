package br.com.joaovitor.gestaomanutencao.pdf;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinaDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMaquinasResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMensalDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetorDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetoresResponseDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioGastoRealizadoDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoAnualDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioOrcamentoMensalDTO;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioPdfBuilderTest {

    private final RelatorioPdfBuilder builder = new RelatorioPdfBuilder();

    @Test
    void custoMensalGeraPdfValidoComTotalFormatado() throws Exception {
        RelatorioCustoMensalDTO dto = new RelatorioCustoMensalDTO(
                3, 2026, new BigDecimal("1940.52"), new BigDecimal("798.26"), new BigDecimal("2738.78")
        );

        byte[] pdf = builder.custoMensal(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$");
        assertThat(texto).contains("R$ 2.738,78");
        assertThat(texto).doesNotContain("2738.78");
        assertThat(texto).contains("Como ler este relatório");
    }

    @Test
    void custoMensalComTudoZeroExibeMensagemDeSemCustos() throws Exception {
        RelatorioCustoMensalDTO dto = new RelatorioCustoMensalDTO(
                3, 2026, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        byte[] pdf = builder.custoMensal(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("Sem custos no período");
        assertThat(texto).contains("R$ 0,00");
    }

    @Test
    void orcamentoMensalGeraPdfValidoComTotalFormatado() throws Exception {
        RelatorioOrcamentoMensalDTO dto = new RelatorioOrcamentoMensalDTO(
                6, 2026, new BigDecimal("10000.00"), new BigDecimal("4872.34"),
                new BigDecimal("5127.66"), new BigDecimal("48.72")
        );

        byte[] pdf = builder.orcamentoMensal(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 4.872,34");
        assertThat(texto).contains("48,72%");
        assertThat(texto).doesNotContain("4872.34");
    }

    @Test
    void orcamentoAnualGeraPdfValidoComTotalFormatado() throws Exception {
        RelatorioOrcamentoAnualDTO dto = new RelatorioOrcamentoAnualDTO(
                2026, new BigDecimal("120000.00"), new BigDecimal("58432.10"),
                new BigDecimal("61567.90"), new BigDecimal("48.69")
        );

        byte[] pdf = builder.orcamentoAnual(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 58.432,10");
        assertThat(texto).doesNotContain("58432.10");
    }

    @Test
    void gastoRealizadoGeraPdfValidoComTotalFormatado() throws Exception {
        RelatorioGastoRealizadoDTO dto = new RelatorioGastoRealizadoDTO(
                9, 2026, new BigDecimal("3200.50"), new BigDecimal("1500.00"), new BigDecimal("4700.50")
        );

        byte[] pdf = builder.gastoRealizado(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 4.700,50");
        assertThat(texto).doesNotContain("4700.50");
    }

    @Test
    void gastoRealizadoComTudoZeroExibeMensagemDeSemCustos() throws Exception {
        RelatorioGastoRealizadoDTO dto = new RelatorioGastoRealizadoDTO(
                9, 2026, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        byte[] pdf = builder.gastoRealizado(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("Sem custos no período");
    }

    @Test
    void custoMaquinaMensalGeraPdfValido() throws Exception {
        RelatorioCustoMaquinaDTO dto = new RelatorioCustoMaquinaDTO(
                1L, "MAQ-001", 4, 2026, new BigDecimal("500.00"), new BigDecimal("300.00"), new BigDecimal("800.00")
        );

        byte[] pdf = builder.custoMaquinaMensal(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 800,00");
        assertThat(texto).contains("MAQ-001");
        assertThat(texto).doesNotContain("800.00").doesNotContain("800.0000");
    }

    @Test
    void custoMaquinaAnualGeraPdfValido() throws Exception {
        RelatorioCustoMaquinaDTO dto = new RelatorioCustoMaquinaDTO(
                1L, "MAQ-002", null, 2026, new BigDecimal("1200.00"), new BigDecimal("600.00"), new BigDecimal("1800.00")
        );

        byte[] pdf = builder.custoMaquinaAnual(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 1.800,00");
        assertThat(texto).contains("Ano: 2026");
    }

    @Test
    void custoMaquinaTotalGeraPdfValido() throws Exception {
        RelatorioCustoMaquinaDTO dto = new RelatorioCustoMaquinaDTO(
                1L, "MAQ-003", null, null, new BigDecimal("900.00"), new BigDecimal("100.00"), new BigDecimal("1000.00")
        );

        byte[] pdf = builder.custoMaquinaTotal(dto);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 1.000,00");
        assertThat(texto).contains("Total acumulado");
    }

    @Test
    void custoMaquinasGeraPdfValidoComTotalGeral() throws Exception {
        List<RelatorioCustoMaquinaDTO> maquinas = List.of(
                new RelatorioCustoMaquinaDTO(1L, "MAQ-001", 5, 2026, new BigDecimal("100.00"), new BigDecimal("50.00"), new BigDecimal("150.00")),
                new RelatorioCustoMaquinaDTO(2L, "MAQ-002", 5, 2026, new BigDecimal("200.00"), new BigDecimal("150.00"), new BigDecimal("350.00"))
        );
        RelatorioCustoMaquinasResponseDTO dto = new RelatorioCustoMaquinasResponseDTO(maquinas, new BigDecimal("500.00"));

        byte[] pdf = builder.custoMaquinas(dto, 5, 2026);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 500,00");
        assertThat(texto).contains("MAQ-001").contains("MAQ-002");
        assertThat(texto).doesNotContain("500.00").doesNotContain("500.0000");
    }

    @Test
    void custoMaquinasComMaisDeDezExibeNotaDeTop10() throws Exception {
        List<RelatorioCustoMaquinaDTO> maquinas = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 1; i <= 12; i++) {
            BigDecimal valor = BigDecimal.valueOf(100 * i);
            maquinas.add(new RelatorioCustoMaquinaDTO(
                    (long) i, "MAQ-" + i, null, null, valor, BigDecimal.ZERO, valor
            ));
            total = total.add(valor);
        }
        RelatorioCustoMaquinasResponseDTO dto = new RelatorioCustoMaquinasResponseDTO(maquinas, total);

        byte[] pdf = builder.custoMaquinas(dto, null, null);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("Exibindo as 10 maiores de 12 máquinas.");
    }

    @Test
    void custoSetoresGeraPdfValidoComPercentual() throws Exception {
        List<RelatorioCustoSetorDTO> setores = List.of(
                new RelatorioCustoSetorDTO(1L, "Produção", true, 3, new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("1500.00"), new BigDecimal("60.00")),
                new RelatorioCustoSetorDTO(2L, "Manutenção", true, 2, new BigDecimal("600.00"), new BigDecimal("400.00"), new BigDecimal("1000.00"), new BigDecimal("40.00"))
        );
        RelatorioCustoSetoresResponseDTO dto = new RelatorioCustoSetoresResponseDTO(4, 2026, setores, new BigDecimal("2500.00"));

        byte[] pdf = builder.custoSetores(dto, List.of(1L, 2L));
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("R$ 2.500,00");
        assertThat(texto).contains("60,00%");
        assertThat(texto).contains("Produção").contains("Manutenção");
        assertThat(texto).contains("Sim");
        assertThat(texto).doesNotContain("2500.00").doesNotContain("2500.0000");
    }

    @Test
    void custoSetoresSemFiltroIndicaTodosOsSetores() throws Exception {
        List<RelatorioCustoSetorDTO> setores = List.of(
                new RelatorioCustoSetorDTO(1L, "Produção", true, 3, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        );
        RelatorioCustoSetoresResponseDTO dto = new RelatorioCustoSetoresResponseDTO(null, null, setores, BigDecimal.ZERO);

        byte[] pdf = builder.custoSetores(dto, null);
        String texto = extrairTexto(pdf);

        assertBytesPdfValidos(pdf);
        assertThat(texto).contains("Nenhum (todos os setores)");
        assertThat(texto).contains("Sem custos no período");
    }

    private void assertBytesPdfValidos(byte[] pdf) {
        assertThat(pdf).isNotEmpty();
        String cabecalho = new String(pdf, 0, 4, StandardCharsets.US_ASCII);
        assertThat(cabecalho).isEqualTo("%PDF");
    }

    private String extrairTexto(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            StringBuilder texto = new StringBuilder();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
                texto.append(extractor.getTextFromPage(pagina)).append('\n');
            }
            return texto.toString();
        } finally {
            reader.close();
        }
    }
}
