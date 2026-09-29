package br.com.joaovitor.gestaomanutencao.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public final class FormatadorRelatorio {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private FormatadorRelatorio() {
    }

    public static String moeda(BigDecimal valor) {
        BigDecimal valorEscalado = (valor == null ? BigDecimal.ZERO : valor)
                .setScale(2, RoundingMode.HALF_UP);
        return "R$ " + criarFormato("#,##0.00").format(valorEscalado);
    }

    public static String percentual(BigDecimal valor) {
        BigDecimal valorEscalado = (valor == null ? BigDecimal.ZERO : valor)
                .setScale(2, RoundingMode.HALF_UP);
        return criarFormato("#,##0.00").format(valorEscalado) + "%";
    }

    public static String simNao(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? "Sim" : "Não";
    }

    public static String periodo(Integer mes, Integer ano) {
        if (mes != null && ano != null) {
            return nomeMes(mes) + "/" + ano;
        }
        if (ano != null) {
            return "Ano: " + ano;
        }
        return "Total acumulado";
    }

    public static String nomeMes(int mes) {
        String nome = Month.of(mes).getDisplayName(TextStyle.FULL, PT_BR);
        return nome.substring(0, 1).toUpperCase(PT_BR) + nome.substring(1);
    }

    private static DecimalFormat criarFormato(String padrao) {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(PT_BR);
        return new DecimalFormat(padrao, simbolos);
    }
}
