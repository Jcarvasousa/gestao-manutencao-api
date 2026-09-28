package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.dto.PecaUsadaResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.DevolucaoExcedeSaldoException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueDevolucaoTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @InjectMocks
    private MovimentacaoEstoqueService service;

    @Test
    void devolucaoDeveLancarExcecaoQuandoManutencaoNaoExistir() {
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.registrarDevolucao(1L, 2L, 1, "x"));
    }

    @Test
    void devolucaoDeveLancarExcecaoQuandoPecaNaoExistir() {
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao(StatusManutencao.ABERTA)));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.registrarDevolucao(1L, 2L, 1, "x"));
    }

    @Test
    void devolucaoDeveLancarExcecaoQuandoManutencaoEstiverFinalizada() {
        for (StatusManutencao status : List.of(StatusManutencao.CONCLUIDA, StatusManutencao.CANCELADA)) {
            when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao(status)));
            when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca(5)));

            assertThrows(ManutencaoNaoEstaAbertaException.class, () -> service.registrarDevolucao(1L, 2L, 1, "x"));
        }
        verify(movimentacaoEstoqueRepository, never()).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void devolucaoAcimaDoSaldoDeveLancarExcecaoInformandoMaximoDevolvivel() {
        Peca peca = peca(0);
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao(StatusManutencao.EM_ANDAMENTO)));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca));
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(2L, 1L))
                .thenReturn(List.of(saida(peca, 3, "10.00")));

        DevolucaoExcedeSaldoException excecao = assertThrows(DevolucaoExcedeSaldoException.class,
                () -> service.registrarDevolucao(1L, 2L, 4, "x"));

        assertTrue(excecao.getMessage().contains("Máximo devolvível: 3"));
        assertEquals(0, peca.getQuantidadeAtual());
        verify(movimentacaoEstoqueRepository, never()).save(any(MovimentacaoEstoque.class));
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void devolucaoSemSaidaPreviaDeveExcederSaldoZero() {
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao(StatusManutencao.ABERTA)));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca(5)));
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(2L, 1L)).thenReturn(List.of());

        DevolucaoExcedeSaldoException excecao = assertThrows(DevolucaoExcedeSaldoException.class,
                () -> service.registrarDevolucao(1L, 2L, 1, "x"));

        assertTrue(excecao.getMessage().contains("Máximo devolvível: 0"));
    }

    @Test
    void devolucaoParcialDeveGravarUmaLinhaSomarEstoqueENaoAlterarCustoDaPeca() {
        Peca peca = peca(0);
        peca.setCustoUnitario(new BigDecimal("99.00"));
        Manutencao manutencao = manutencao(StatusManutencao.EM_ANDAMENTO);
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca));
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(2L, 1L))
                .thenReturn(List.of(saida(peca, 5, "10.00")));
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimentacaoEstoque> resultado = service.registrarDevolucao(1L, 2L, 2, "Sobrou");

        assertEquals(1, resultado.size());
        MovimentacaoEstoque devolucao = resultado.get(0);
        assertEquals(TipoMovimentacao.DEVOLUCAO, devolucao.getTipo());
        assertEquals(2, devolucao.getQuantidade());
        assertEquals(new BigDecimal("10.00"), devolucao.getCustoUnitarioMomento());
        assertEquals(manutencao, devolucao.getManutencao());
        assertEquals("Sobrou", devolucao.getObservacao());
        assertEquals(2, peca.getQuantidadeAtual());
        assertEquals(new BigDecimal("99.00"), peca.getCustoUnitario());
        verify(pecaRepository).save(peca);
    }

    @Test
    void devolucaoDeveConsumirLotesEmOrdemLifoComCustoExatoDeCadaLote() {
        Peca peca = peca(0);
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao(StatusManutencao.ABERTA)));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca));
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(2L, 1L))
                .thenReturn(List.of(saida(peca, 2, "10.00"), saida(peca, 3, "20.00")));
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimentacaoEstoque> resultado = service.registrarDevolucao(1L, 2L, 4, "x");

        assertEquals(2, resultado.size());
        assertEquals(3, resultado.get(0).getQuantidade());
        assertEquals(new BigDecimal("20.00"), resultado.get(0).getCustoUnitarioMomento());
        assertEquals(1, resultado.get(1).getQuantidade());
        assertEquals(new BigDecimal("10.00"), resultado.get(1).getCustoUnitarioMomento());
        assertEquals(4, peca.getQuantidadeAtual());
    }

    @Test
    void devolucaoTotalEmEtapasDeveZerarSaldoECustoExatamente() {
        Peca peca = peca(0);
        Manutencao manutencao = manutencao(StatusManutencao.ABERTA);
        List<MovimentacaoEstoque> historico = new ArrayList<>(List.of(
                saida(peca, 2, "10.00"), saida(peca, 3, "20.00")
        ));
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca));
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(2L, 1L)).thenReturn(historico);
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        historico.addAll(service.registrarDevolucao(1L, 2L, 4, "primeira"));
        historico.addAll(service.registrarDevolucao(1L, 2L, 1, "segunda"));

        assertEquals(5, peca.getQuantidadeAtual());
        assertThrows(DevolucaoExcedeSaldoException.class, () -> service.registrarDevolucao(1L, 2L, 1, "terceira"));

        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencao(2L)).thenReturn(historico);
        when(manutencaoRepository.existsById(2L)).thenReturn(true);
        assertTrue(service.listarPecasUsadas(2L).isEmpty());
    }

    @Test
    void devolucaoDeLoteSemCustoDeveGravarCustoNulo() {
        Peca peca = peca(0);
        when(manutencaoRepository.buscarPorIdComTrava(2L)).thenReturn(Optional.of(manutencao(StatusManutencao.ABERTA)));
        when(pecaRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(peca));
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencaoEPeca(2L, 1L))
                .thenReturn(List.of(saida(peca, 2, null), saida(peca, 1, "5.00")));
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimentacaoEstoque> resultado = service.registrarDevolucao(1L, 2L, 3, "x");

        assertEquals(2, resultado.size());
        assertEquals(new BigDecimal("5.00"), resultado.get(0).getCustoUnitarioMomento());
        assertNull(resultado.get(1).getCustoUnitarioMomento());
        assertEquals(2, resultado.get(1).getQuantidade());
    }

    @Test
    void listarPecasUsadasDeveSomarLotesRemanescentesIgnorandoLotesSemCusto() {
        Peca peca = peca(0);
        peca.setCodigo("PEC-001");
        peca.setNome("Rolamento");
        List<MovimentacaoEstoque> historico = List.of(
                saida(peca, 2, null),
                saida(peca, 3, "20.00"),
                devolucao(peca, 1, "20.00")
        );
        when(manutencaoRepository.existsById(2L)).thenReturn(true);
        when(movimentacaoEstoqueRepository.buscarSaidasEDevolucoesPorManutencao(2L)).thenReturn(historico);

        List<PecaUsadaResponseDTO> resultado = service.listarPecasUsadas(2L);

        assertEquals(1, resultado.size());
        assertEquals(4, resultado.get(0).quantidadeUsada());
        assertEquals(4, resultado.get(0).saldoDevolvivel());
        assertEquals(new BigDecimal("40.00"), resultado.get(0).custoTotal());
    }

    @Test
    void listarPecasUsadasDeveLancarExcecaoQuandoManutencaoNaoExistir() {
        when(manutencaoRepository.existsById(2L)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.listarPecasUsadas(2L));
    }

    private Peca peca(int quantidadeAtual) {
        Peca peca = new Peca();
        peca.setId(1L);
        peca.setQuantidadeAtual(quantidadeAtual);
        return peca;
    }

    private Manutencao manutencao(StatusManutencao status) {
        Manutencao manutencao = new Manutencao();
        manutencao.setId(2L);
        manutencao.setStatus(status);
        return manutencao;
    }

    private MovimentacaoEstoque saida(Peca peca, int quantidade, String custo) {
        return movimentacao(TipoMovimentacao.SAIDA, peca, quantidade, custo);
    }

    private MovimentacaoEstoque devolucao(Peca peca, int quantidade, String custo) {
        return movimentacao(TipoMovimentacao.DEVOLUCAO, peca, quantidade, custo);
    }

    private MovimentacaoEstoque movimentacao(TipoMovimentacao tipo, Peca peca, int quantidade, String custo) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setPeca(peca);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setCustoUnitarioMomento(custo == null ? null : new BigDecimal(custo));
        return movimentacao;
    }
}
