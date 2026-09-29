package br.com.joaovitor.gestaomanutencao.pdf;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FormatadorRelatorioTest {

    @Test
    void formataValorMonetarioComSeparadoresPtBr() {
        assertThat(FormatadorRelatorio.moeda(new BigDecimal("2738.78"))).isEqualTo("R$ 2.738,78");
    }

    @Test
    void formataValorMonetarioZeroENulo() {
        assertThat(FormatadorRelatorio.moeda(BigDecimal.ZERO)).isEqualTo("R$ 0,00");
        assertThat(FormatadorRelatorio.moeda(null)).isEqualTo("R$ 0,00");
    }

    @Test
    void arredondaValorMonetarioComHalfUp() {
        assertThat(FormatadorRelatorio.moeda(new BigDecimal("1940.525"))).isEqualTo("R$ 1.940,53");
    }

    @Test
    void formataPercentualComVirgulaEDoisDigitos() {
        assertThat(FormatadorRelatorio.percentual(new BigDecimal("48.7234"))).isEqualTo("48,72%");
    }

    @Test
    void formataSimNao() {
        assertThat(FormatadorRelatorio.simNao(Boolean.TRUE)).isEqualTo("Sim");
        assertThat(FormatadorRelatorio.simNao(Boolean.FALSE)).isEqualTo("Não");
        assertThat(FormatadorRelatorio.simNao(null)).isEqualTo("Não");
    }

    @Test
    void formataPeriodoMensal() {
        assertThat(FormatadorRelatorio.periodo(3, 2026)).isEqualTo("Março/2026");
    }

    @Test
    void formataPeriodoAnual() {
        assertThat(FormatadorRelatorio.periodo(null, 2026)).isEqualTo("Ano: 2026");
    }

    @Test
    void formataPeriodoTotalAcumulado() {
        assertThat(FormatadorRelatorio.periodo(null, null)).isEqualTo("Total acumulado");
    }
}
