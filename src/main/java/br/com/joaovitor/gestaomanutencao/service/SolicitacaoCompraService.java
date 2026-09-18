package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.CompraDesnecessariaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import br.com.joaovitor.gestaomanutencao.repository.SolicitacaoCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SolicitacaoCompraService {

    private final SolicitacaoCompraRepository solicitacaoCompraRepository;
    private final PecaRepository pecaRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final ManutencaoRepository manutencaoRepository;

    public SolicitacaoCompraService(
            SolicitacaoCompraRepository solicitacaoCompraRepository,
            PecaRepository pecaRepository,
            MovimentacaoEstoqueService movimentacaoEstoqueService,
            ManutencaoRepository manutencaoRepository
    ) {
        this.solicitacaoCompraRepository = solicitacaoCompraRepository;
        this.pecaRepository = pecaRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
        this.manutencaoRepository = manutencaoRepository;
    }

    @Transactional
    public SolicitacaoCompra criar(
            Long pecaId,
            Long manutencaoId,
            Integer quantidade,
            String fornecedor,
            BigDecimal valorOrcamento
    ) {
        Peca peca = pecaRepository.findById(pecaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça não encontrada para o id: " + pecaId));

        if (peca.getQuantidadeAtual() >= quantidade) {
            throw new CompraDesnecessariaException(
                    "Compra desnecessária. Quantidade disponível: "
                            + peca.getQuantidadeAtual()
                            + ", quantidade solicitada: "
                            + quantidade
            );
        }

        Manutencao manutencao = null;
        if (manutencaoId != null) {
            manutencao = manutencaoRepository.findById(manutencaoId).orElse(null);
        }

        SolicitacaoCompra solicitacaoCompra = new SolicitacaoCompra();
        solicitacaoCompra.setPeca(peca);
        solicitacaoCompra.setManutencao(manutencao);
        solicitacaoCompra.setQuantidade(quantidade);
        solicitacaoCompra.setFornecedor(fornecedor);
        solicitacaoCompra.setValorOrcamento(valorOrcamento);

        return solicitacaoCompraRepository.save(solicitacaoCompra);
    }

    @Transactional
    public SolicitacaoCompra marcarComoRecebida(Long solicitacaoCompraId) {
        SolicitacaoCompra solicitacaoCompra = solicitacaoCompraRepository.findById(solicitacaoCompraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Solicitação de compra não encontrada para o id: " + solicitacaoCompraId
                ));

        movimentacaoEstoqueService.registrarEntrada(
                solicitacaoCompra.getPeca().getId(),
                solicitacaoCompra.getQuantidade(),
                "Entrada referente à solicitação de compra #" + solicitacaoCompraId
        );

        solicitacaoCompra.setStatus(StatusSolicitacaoCompra.RECEBIDA);
        solicitacaoCompra.setDataRecebimento(LocalDateTime.now());

        return solicitacaoCompraRepository.save(solicitacaoCompra);
    }
}