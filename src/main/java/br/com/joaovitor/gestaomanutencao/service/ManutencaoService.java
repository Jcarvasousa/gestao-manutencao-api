package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.dto.PecaUsadaResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public ManutencaoService(
            ManutencaoRepository manutencaoRepository,
            MovimentacaoEstoqueService movimentacaoEstoqueService
    ) {
        this.manutencaoRepository = manutencaoRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    @Transactional
    public Manutencao cancelar(Long id) {
        Manutencao manutencao = manutencaoRepository.buscarPorIdComTrava(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção com ID " + id + " não encontrada."));

        if (manutencao.estaFinalizada()) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível cancelar manutenção com status: " + manutencao.getStatus()
            );
        }

        // listarPecasUsadas devolve as peças em ordem de id, o que mantém a ordem de travas (manutenção -> peças por id)
        for (PecaUsadaResponseDTO pecaUsada : movimentacaoEstoqueService.listarPecasUsadas(id)) {
            movimentacaoEstoqueService.registrarDevolucao(
                    pecaUsada.pecaId(),
                    id,
                    pecaUsada.saldoDevolvivel(),
                    "Devolução automática por cancelamento da manutenção #" + id
            );
        }

        manutencao.setStatus(StatusManutencao.CANCELADA);
        manutencaoRepository.save(manutencao);
        return manutencao;
    }
}
