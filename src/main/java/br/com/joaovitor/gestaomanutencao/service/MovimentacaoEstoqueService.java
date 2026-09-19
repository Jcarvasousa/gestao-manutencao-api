package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.EstoqueInsuficienteException;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final PecaRepository pecaRepository;
    private final ManutencaoRepository manutencaoRepository;

    public MovimentacaoEstoqueService(
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            PecaRepository pecaRepository,
            ManutencaoRepository manutencaoRepository
    ) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.pecaRepository = pecaRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    @Transactional
    public MovimentacaoEstoque registrarSaida(Long pecaId, Long manutencaoId, Integer quantidade, String observacao) {
        Peca peca = pecaRepository.findById(pecaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada para o id: " + pecaId));

        Manutencao manutencao = manutencaoRepository.findById(manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção não encontrada para o id: " + manutencaoId));

        if (manutencao.getStatus() == StatusManutencao.CONCLUIDA || manutencao.getStatus() == StatusManutencao.CANCELADA) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível registrar saída para manutenção com status: " + manutencao.getStatus()
            );
        }

        if (peca.getQuantidadeAtual() < quantidade) {
            throw new EstoqueInsuficienteException(
                    "Estoque insuficiente para a peça de id "
                            + pecaId
                            + ". Disponível: "
                            + peca.getQuantidadeAtual()
                            + ", solicitado: "
                            + quantidade
            );
        }

        BigDecimal custoNoMomento = peca.getCustoUnitario();

        peca.setQuantidadeAtual(peca.getQuantidadeAtual() - quantidade);
        pecaRepository.save(peca);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setPeca(peca);
        movimentacao.setTipo(TipoMovimentacao.SAIDA);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setCustoUnitarioMomento(custoNoMomento);
        movimentacao.setManutencao(manutencao);
        movimentacao.setObservacao(observacao);

        return movimentacaoEstoqueRepository.save(movimentacao);
    }

    @Transactional
    public MovimentacaoEstoque registrarEntrada(Long pecaId, Integer quantidade, String observacao) {
        Peca peca = pecaRepository.findById(pecaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada para o id: " + pecaId));

        peca.setQuantidadeAtual(peca.getQuantidadeAtual() + quantidade);
        pecaRepository.save(peca);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setPeca(peca);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setManutencao(null);
        movimentacao.setObservacao(observacao);

        return movimentacaoEstoqueRepository.save(movimentacao);
    }
}