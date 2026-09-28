package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Pilha de lotes de saída de um par (manutenção, peça). Cada SAIDA empilha um lote
 * (quantidade, custo do momento); cada DEVOLUCAO desempilha do topo (LIFO).
 */
final class PilhaLotes {

    record Lote(int quantidade, BigDecimal custoUnitario) {
    }

    private final Deque<Lote> lotes = new ArrayDeque<>();

    /** Reconstrói a pilha reaplicando, em ordem, as movimentações SAIDA e DEVOLUCAO. */
    static PilhaLotes reconstruir(List<MovimentacaoEstoque> movimentacoesOrdenadasPorId) {
        PilhaLotes pilha = new PilhaLotes();
        for (MovimentacaoEstoque movimentacao : movimentacoesOrdenadasPorId) {
            if (movimentacao.getTipo() == TipoMovimentacao.SAIDA) {
                pilha.empilhar(movimentacao.getQuantidade(), movimentacao.getCustoUnitarioMomento());
            } else if (movimentacao.getTipo() == TipoMovimentacao.DEVOLUCAO) {
                pilha.desempilhar(movimentacao.getQuantidade());
            }
        }
        return pilha;
    }

    void empilhar(int quantidade, BigDecimal custoUnitario) {
        lotes.push(new Lote(quantidade, custoUnitario));
    }

    /** Remove {@code quantidade} unidades do topo; devolve um Lote por lote (total ou parcialmente) consumido. */
    List<Lote> desempilhar(int quantidade) {
        List<Lote> consumidos = new ArrayList<>();
        int restante = quantidade;
        while (restante > 0 && !lotes.isEmpty()) {
            Lote topo = lotes.pop();
            int retirado = Math.min(topo.quantidade(), restante);
            consumidos.add(new Lote(retirado, topo.custoUnitario()));
            if (retirado < topo.quantidade()) {
                lotes.push(new Lote(topo.quantidade() - retirado, topo.custoUnitario()));
            }
            restante -= retirado;
        }
        return consumidos;
    }

    int total() {
        return lotes.stream().mapToInt(Lote::quantidade).sum();
    }

    /** Custo dos lotes remanescentes; lotes sem custo são ignorados. */
    BigDecimal custoTotal() {
        return lotes.stream()
                .filter(lote -> lote.custoUnitario() != null)
                .map(lote -> lote.custoUnitario().multiply(BigDecimal.valueOf(lote.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
