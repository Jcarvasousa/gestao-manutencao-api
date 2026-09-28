package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.CompraDesnecessariaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.SolicitacaoJaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.SolicitacaoNaoPodeSerRecebidaException;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusSolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import br.com.joaovitor.gestaomanutencao.repository.SolicitacaoCompraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoCompraServiceTest {

    @Mock
    private SolicitacaoCompraRepository solicitacaoCompraRepository;

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @InjectMocks
    private SolicitacaoCompraService service;

    @Test
    void criarDeveLancarExcecaoQuandoPecaNaoExistir() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criar(1L, null, 5, "Fornecedor", null));
    }

    @Test
    void criarDeveLancarExcecaoQuandoEstoqueForSuficiente() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca(5)));

        assertThrows(CompraDesnecessariaException.class,
                () -> service.criar(1L, null, 5, "Fornecedor", null));
    }

    @Test
    void criarDeveSalvarSolicitacaoQuandoEstoqueForInsuficiente() {
        Peca peca = peca(2);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoCompra resultado = service.criar(
                1L, null, 5, "Fornecedor", null
        );

        assertNotNull(resultado);
        assertEquals(peca, resultado.getPeca());
        assertEquals(5, resultado.getQuantidadeNecessaria());
        assertEquals("Fornecedor", resultado.getFornecedor());
        assertEquals(StatusSolicitacaoCompra.AGUARDANDO_ORCAMENTO, resultado.getStatus());
        verify(solicitacaoCompraRepository).save(resultado);
    }

    @Test
    void criarDeveLancarExcecaoQuandoSolicitacaoAguardandoOrcamentoJaExistir() {
        Peca peca = peca(2);
        SolicitacaoCompra existente = solicitacao(10L, StatusSolicitacaoCompra.AGUARDANDO_ORCAMENTO);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(solicitacaoCompraRepository.findFirstByPecaIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.of(existente));

        assertThrows(SolicitacaoJaAbertaException.class,
                () -> service.criar(1L, null, 5, "Fornecedor", null));
    }

    @Test
    void criarDeveLancarExcecaoQuandoSolicitacaoAprovadaJaExistir() {
        Peca peca = peca(2);
        SolicitacaoCompra existente = solicitacao(11L, StatusSolicitacaoCompra.APROVADA);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(solicitacaoCompraRepository.findFirstByPecaIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.of(existente));

        assertThrows(SolicitacaoJaAbertaException.class,
                () -> service.criar(1L, null, 5, "Fornecedor", null));
    }

    @Test
    void criarDevePermitirNovaSolicitacaoQuandoAnteriorFoiRecebida() {
        Peca peca = peca(2);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(solicitacaoCompraRepository.findFirstByPecaIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.empty());
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoCompra resultado = service.criar(1L, null, 5, "Fornecedor", null);

        assertNotNull(resultado);
        verify(solicitacaoCompraRepository).save(resultado);
    }

    @Test
    void criarDevePermitirNovaSolicitacaoQuandoAnteriorFoiCancelada() {
        Peca peca = peca(2);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(solicitacaoCompraRepository.findFirstByPecaIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Optional.empty());
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoCompra resultado = service.criar(1L, null, 5, "Fornecedor", null);

        assertNotNull(resultado);
        verify(solicitacaoCompraRepository).save(resultado);
    }

    @Test
    void marcarComoRecebidaDeveLancarExcecaoQuandoSolicitacaoNaoExistir() {
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.marcarComoRecebida(1L, new BigDecimal("100.00")));
    }

    @Test
    void marcarComoRecebidaDeveRegistrarEntradaEAtualizarStatus() {
        Peca peca = peca(0);
        SolicitacaoCompra solicitacao = new SolicitacaoCompra();
        solicitacao.setPeca(peca);
        solicitacao.setQuantidadeNecessaria(4);
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoCompra resultado = service.marcarComoRecebida(1L, new BigDecimal("100.00"));

        assertNotNull(resultado);
        assertEquals(StatusSolicitacaoCompra.RECEBIDA, resultado.getStatus());
        assertNotNull(resultado.getDataRecebimento());
        verify(movimentacaoEstoqueService).registrarEntrada(
                1L, 4, "Entrada referente à solicitação de compra #1"
        );
        verify(solicitacaoCompraRepository).save(solicitacao);
    }

    @Test
    void marcarComoRecebidaDeveGravarValorPagoEReceber() {
        Peca peca = peca(0);
        SolicitacaoCompra solicitacao = solicitacao(1L, StatusSolicitacaoCompra.PEDIDO_REALIZADO);
        solicitacao.setPeca(peca);
        solicitacao.setQuantidadeNecessaria(4);
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoCompra resultado = service.marcarComoRecebida(1L, new BigDecimal("250.50"));

        assertEquals(new BigDecimal("250.50"), resultado.getValorOrcamento());
        assertEquals(StatusSolicitacaoCompra.RECEBIDA, resultado.getStatus());
        assertNotNull(resultado.getDataRecebimento());
        verify(movimentacaoEstoqueService).registrarEntrada(
                1L, 4, "Entrada referente à solicitação de compra #1"
        );
    }

    @Test
    void marcarComoRecebidaDeveLancarExcecaoQuandoStatusForRecebida() {
        SolicitacaoCompra solicitacao = solicitacao(1L, StatusSolicitacaoCompra.RECEBIDA);
        solicitacao.setPeca(peca(0));
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.of(solicitacao));

        assertThrows(SolicitacaoNaoPodeSerRecebidaException.class,
                () -> service.marcarComoRecebida(1L, new BigDecimal("100.00")));

        verify(movimentacaoEstoqueService, never()).registrarEntrada(anyLong(), anyInt(), anyString());
        verify(solicitacaoCompraRepository, never()).save(any(SolicitacaoCompra.class));
    }

    @Test
    void marcarComoRecebidaDeveLancarExcecaoQuandoStatusForCancelada() {
        SolicitacaoCompra solicitacao = solicitacao(1L, StatusSolicitacaoCompra.CANCELADA);
        solicitacao.setPeca(peca(0));
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.of(solicitacao));

        assertThrows(SolicitacaoNaoPodeSerRecebidaException.class,
                () -> service.marcarComoRecebida(1L, new BigDecimal("100.00")));

        verify(movimentacaoEstoqueService, never()).registrarEntrada(anyLong(), anyInt(), anyString());
        verify(solicitacaoCompraRepository, never()).save(any(SolicitacaoCompra.class));
    }

    @Test
    void marcarComoRecebidaDeveGravarCustoUnitarioNaPeca() {
        Peca peca = peca(0);
        SolicitacaoCompra solicitacao = solicitacaoPendente(peca, 15);
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.marcarComoRecebida(1L, new BigDecimal("800.00"));

        assertEquals(new BigDecimal("53.33"), peca.getCustoUnitario());
        verify(pecaRepository).save(peca);
    }

    @Test
    void marcarComoRecebidaDeveArredondarCustoUnitarioParaBaixoEParaCima() {
        Peca pecaBaixo = peca(0);
        when(solicitacaoCompraRepository.findById(1L))
                .thenReturn(Optional.of(solicitacaoPendente(pecaBaixo, 3)));
        when(solicitacaoCompraRepository.save(any(SolicitacaoCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.marcarComoRecebida(1L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("33.33"), pecaBaixo.getCustoUnitario());

        Peca pecaCima = peca(0);
        when(solicitacaoCompraRepository.findById(2L))
                .thenReturn(Optional.of(solicitacaoPendente(pecaCima, 3)));

        service.marcarComoRecebida(2L, new BigDecimal("200.00"));

        assertEquals(new BigDecimal("66.67"), pecaCima.getCustoUnitario());
    }

    @Test
    void marcarComoRecebidaNaoDeveAlterarCustoQuandoStatusForRecebida() {
        assertCustoPreservadoParaStatus(StatusSolicitacaoCompra.RECEBIDA);
    }

    @Test
    void marcarComoRecebidaNaoDeveAlterarCustoQuandoStatusForCancelada() {
        assertCustoPreservadoParaStatus(StatusSolicitacaoCompra.CANCELADA);
    }

    private void assertCustoPreservadoParaStatus(StatusSolicitacaoCompra status) {
        Peca peca = peca(0);
        peca.setCustoUnitario(new BigDecimal("10.00"));
        SolicitacaoCompra solicitacao = solicitacao(1L, status);
        solicitacao.setPeca(peca);
        solicitacao.setQuantidadeNecessaria(15);
        when(solicitacaoCompraRepository.findById(1L)).thenReturn(Optional.of(solicitacao));

        assertThrows(SolicitacaoNaoPodeSerRecebidaException.class,
                () -> service.marcarComoRecebida(1L, new BigDecimal("800.00")));

        assertEquals(new BigDecimal("10.00"), peca.getCustoUnitario());
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    private SolicitacaoCompra solicitacaoPendente(Peca peca, int quantidadeNecessaria) {
        SolicitacaoCompra solicitacao = solicitacao(1L, StatusSolicitacaoCompra.PEDIDO_REALIZADO);
        solicitacao.setPeca(peca);
        solicitacao.setQuantidadeNecessaria(quantidadeNecessaria);
        return solicitacao;
    }

    private Peca peca(int quantidadeAtual) {
        Peca peca = new Peca();
        peca.setId(1L);
        peca.setQuantidadeAtual(quantidadeAtual);
        return peca;
    }

    private SolicitacaoCompra solicitacao(Long id, StatusSolicitacaoCompra status) {
        SolicitacaoCompra solicitacao = new SolicitacaoCompra();
        solicitacao.setId(id);
        solicitacao.setStatus(status);
        return solicitacao;
    }
}
