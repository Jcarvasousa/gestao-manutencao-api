package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.dto.PecaUsadaResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.AjusteEstoqueInvalidoException;
import br.com.joaovitor.gestaomanutencao.exception.DevolucaoExcedeSaldoException;
import br.com.joaovitor.gestaomanutencao.exception.EstoqueInsuficienteException;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.QuantidadeInvalidaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Manutencao manutencao = manutencaoRepository.buscarPorIdComTrava(manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção não encontrada para o id: " + manutencaoId));

        Peca peca = pecaRepository.buscarPorIdComTrava(pecaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada para o id: " + pecaId));

        if (manutencao.estaFinalizada()) {
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

    @Transactional
    public MovimentacaoEstoque registrarAjuste(Long pecaId, Integer quantidadeNova, String observacao) {
        Peca peca = pecaRepository.findById(pecaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada para o id: " + pecaId));

        int diferenca = quantidadeNova - peca.getQuantidadeAtual();
        if (diferenca == 0) {
            throw new AjusteEstoqueInvalidoException(
                    "A quantidade nova informada é igual à quantidade atual da peça de id " + pecaId
                            + ". Nenhum ajuste é necessário."
            );
        }

        peca.setQuantidadeAtual(quantidadeNova);
        pecaRepository.save(peca);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setPeca(peca);
        movimentacao.setTipo(TipoMovimentacao.AJUSTE);
        movimentacao.setQuantidade(Math.abs(diferenca));
        movimentacao.setCustoUnitarioMomento(null);
        movimentacao.setManutencao(null);
        movimentacao.setObservacao(observacao);

        return movimentacaoEstoqueRepository.save(movimentacao);
    }

    @Transactional
    public List<MovimentacaoEstoque> registrarDevolucao(Long pecaId, Long manutencaoId, Integer quantidade, String observacao) {
        if (quantidade == null || quantidade < 1) {
            throw new QuantidadeInvalidaException("A quantidade a devolver deve ser de pelo menos 1.");
        }

        Manutencao manutencao = manutencaoRepository.buscarPorIdComTrava(manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção não encontrada para o id: " + manutencaoId));

        Peca peca = pecaRepository.buscarPorIdComTrava(pecaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada para o id: " + pecaId));

        if (manutencao.estaFinalizada()) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível registrar devolução para manutenção com status: " + manutencao.getStatus()
            );
        }

        PilhaLotes pilha = PilhaLotes.reconstruir(
                movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(manutencaoId, pecaId)
        );

        if (quantidade > pilha.total()) {
            throw new DevolucaoExcedeSaldoException(
                    "Devolução acima do saldo devolvível da peça de id "
                            + pecaId
                            + " na manutenção de id "
                            + manutencaoId
                            + ". Máximo devolvível: "
                            + pilha.total()
                            + ", solicitado: "
                            + quantidade
            );
        }

        List<MovimentacaoEstoque> devolucoes = new ArrayList<>();
        for (PilhaLotes.Lote lote : pilha.desempilhar(quantidade)) {
            MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
            movimentacao.setPeca(peca);
            movimentacao.setTipo(TipoMovimentacao.DEVOLUCAO);
            movimentacao.setQuantidade(lote.quantidade());
            movimentacao.setCustoUnitarioMomento(lote.custoUnitario());
            movimentacao.setManutencao(manutencao);
            movimentacao.setObservacao(observacao);
            devolucoes.add(movimentacaoEstoqueRepository.save(movimentacao));
        }

        peca.setQuantidadeAtual(peca.getQuantidadeAtual() + quantidade);
        pecaRepository.save(peca);

        return devolucoes;
    }

    @Transactional(readOnly = true)
    public List<PecaUsadaResponseDTO> listarPecasUsadas(Long manutencaoId) {
        if (!manutencaoRepository.existsById(manutencaoId)) {
            throw new RecursoNaoEncontradoException("Manutenção não encontrada para o id: " + manutencaoId);
        }

        Map<Long, List<MovimentacaoEstoque>> movimentacoesPorPeca = new LinkedHashMap<>();
        for (MovimentacaoEstoque movimentacao : movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencao(manutencaoId)) {
            movimentacoesPorPeca
                    .computeIfAbsent(movimentacao.getPeca().getId(), id -> new ArrayList<>())
                    .add(movimentacao);
        }

        List<PecaUsadaResponseDTO> pecasUsadas = new ArrayList<>();
        for (List<MovimentacaoEstoque> movimentacoes : movimentacoesPorPeca.values()) {
            PilhaLotes pilha = PilhaLotes.reconstruir(movimentacoes);
            if (pilha.total() > 0) {
                Peca peca = movimentacoes.get(0).getPeca();
                pecasUsadas.add(new PecaUsadaResponseDTO(
                        peca.getId(),
                        peca.getCodigo(),
                        peca.getNome(),
                        peca.getUnidadeMedida(),
                        pilha.total(),
                        pilha.custoTotal(),
                        pilha.total()
                ));
            }
        }
        return pecasUsadas;
    }
}
