package br.com.joaovitor.gestaomanutencao.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class RodapePageEvent extends PdfPageEventHelper {

    private static final Color COR_RODAPE = new Color(107, 114, 128);
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.of("pt", "BR"));

    private final String nomeSistema;
    private final ZonedDateTime dataGeracao;

    private PdfTemplate templateTotalPaginas;
    private BaseFont baseFont;

    RodapePageEvent(String nomeSistema, ZonedDateTime dataGeracao) {
        this.nomeSistema = nomeSistema;
        this.dataGeracao = dataGeracao;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        try {
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Não foi possível carregar a fonte do rodapé.", e);
        }
        templateTotalPaginas = writer.getDirectContent().createTemplate(50, 20);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte conteudo = writer.getDirectContent();
        float y = document.bottom() - 25;

        conteudo.saveState();
        conteudo.setColorFill(COR_RODAPE);

        conteudo.beginText();
        conteudo.setFontAndSize(baseFont, 8);
        conteudo.showTextAligned(PdfContentByte.ALIGN_LEFT, nomeSistema, document.left(), y, 0);
        conteudo.showTextAligned(
                PdfContentByte.ALIGN_RIGHT,
                "Gerado em " + FORMATO_DATA.format(dataGeracao),
                document.right(),
                y,
                0
        );
        conteudo.endText();

        String textoPagina = "Página " + writer.getPageNumber() + " de ";
        float larguraTexto = baseFont.getWidthPoint(textoPagina, 8);
        float centroX = (document.left() + document.right()) / 2;
        float xInicio = centroX - (larguraTexto / 2);

        conteudo.beginText();
        conteudo.setFontAndSize(baseFont, 8);
        conteudo.showTextAligned(PdfContentByte.ALIGN_LEFT, textoPagina, xInicio, y, 0);
        conteudo.endText();

        conteudo.addTemplate(templateTotalPaginas, xInicio + larguraTexto, y);
        conteudo.restoreState();
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        // Em onCloseDocument, o writer já incrementou o contador para a página seguinte
        // (que nunca chega a existir), então o total real de páginas é getPageNumber() - 1.
        int totalPaginas = writer.getPageNumber() - 1;
        templateTotalPaginas.beginText();
        templateTotalPaginas.setFontAndSize(baseFont, 8);
        templateTotalPaginas.setColorFill(COR_RODAPE);
        templateTotalPaginas.setTextMatrix(0, 0);
        templateTotalPaginas.showText(String.valueOf(totalPaginas));
        templateTotalPaginas.endText();
    }
}
