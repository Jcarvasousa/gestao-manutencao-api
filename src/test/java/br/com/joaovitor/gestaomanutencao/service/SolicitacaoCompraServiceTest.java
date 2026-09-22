package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.CompraDesnecessariaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.SolicitacaoJaAbertaException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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
                () -> service.marcarComoRecebida(1L));
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

        SolicitacaoCompra resultado = service.marcarComoRecebida(1L);

        assertNotNull(resultado);
        assertEquals(StatusSolicitacaoCompra.RECEBIDA, resultado.getStatus());
        assertNotNull(resultado.getDataRecebimento());
        verify(movimentacaoEstoqueService).registrarEntrada(
                1L, 4, "Entrada referente à solicitação de compra #1"
        );
        verify(solicitacaoCompraRepository).save(solicitacao);
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
