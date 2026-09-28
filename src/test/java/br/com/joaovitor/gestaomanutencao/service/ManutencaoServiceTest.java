package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.dto.PecaUsadaResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.UnidadeMedida;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @InjectMocks
    private ManutencaoService service;

    @Test
    void cancelarDeveLancarExcecaoQuandoManutencaoNaoExistir() {
        when(manutencaoRepository.buscarPorIdComTrava(7L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.cancelar(7L));
    }

    @Test
    void cancelarDeveBloquearManutencaoConcluidaOuCancelada() {
        for (StatusManutencao status : List.of(StatusManutencao.CONCLUIDA, StatusManutencao.CANCELADA)) {
            Manutencao manutencao = manutencao(status);
            when(manutencaoRepository.buscarPorIdComTrava(7L)).thenReturn(Optional.of(manutencao));

            assertThrows(ManutencaoNaoEstaAbertaException.class, () -> service.cancelar(7L));
            assertEquals(status, manutencao.getStatus());
        }
        verify(movimentacaoEstoqueService, never()).registrarDevolucao(anyLong(), anyLong(), anyInt(), anyString());
        verify(manutencaoRepository, never()).save(any(Manutencao.class));
    }

    @Test
    void cancelarDeveDevolverTodasAsPecasEmOrdemDeIdEMarcarComoCanceladaSemDataDeConclusao() {
        Manutencao manutencao = manutencao(StatusManutencao.EM_ANDAMENTO);
        when(manutencaoRepository.buscarPorIdComTrava(7L)).thenReturn(Optional.of(manutencao));
        when(movimentacaoEstoqueService.listarPecasUsadas(7L)).thenReturn(List.of(
                pecaUsada(1L, 3), pecaUsada(4L, 2)
        ));

        Manutencao resultado = service.cancelar(7L);

        String observacao = "Devolução automática por cancelamento da manutenção #7";
        InOrder ordem = inOrder(movimentacaoEstoqueService);
        ordem.verify(movimentacaoEstoqueService).registrarDevolucao(1L, 7L, 3, observacao);
        ordem.verify(movimentacaoEstoqueService).registrarDevolucao(4L, 7L, 2, observacao);
        assertEquals(StatusManutencao.CANCELADA, resultado.getStatus());
        assertNull(resultado.getDataConclusao());
        verify(manutencaoRepository).save(manutencao);
    }

    private PecaUsadaResponseDTO pecaUsada(Long pecaId, int saldo) {
        return new PecaUsadaResponseDTO(pecaId, "PEC-" + pecaId, "Peça", UnidadeMedida.UN, saldo, BigDecimal.ZERO, saldo);
    }

    private Manutencao manutencao(StatusManutencao status) {
        Manutencao manutencao = new Manutencao();
        manutencao.setId(7L);
        manutencao.setStatus(status);
        return manutencao;
    }
}
