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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @InjectMocks
    private MovimentacaoEstoqueService service;

    @Test
    void registrarSaidaDeveLancarExcecaoQuandoPecaNaoExistir() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.registrarSaida(1L, 2L, 1, "Saída"));
    }

    @Test
    void registrarSaidaDeveLancarExcecaoQuandoManutencaoNaoExistir() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca(5)));
        when(manutencaoRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.registrarSaida(1L, 2L, 1, "Saída"));
    }

    @Test
    void registrarSaidaDeveLancarExcecaoQuandoManutencaoEstiverConcluida() {
        Peca peca = peca(5);
        Manutencao manutencao = manutencao(StatusManutencao.CONCLUIDA);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(manutencaoRepository.findById(2L)).thenReturn(Optional.of(manutencao));

        assertThrows(ManutencaoNaoEstaAbertaException.class,
                () -> service.registrarSaida(1L, 2L, 1, "Saída"));
    }

    @Test
    void registrarSaidaDeveLancarExcecaoQuandoEstoqueForInsuficiente() {
        Peca peca = peca(2);
        Manutencao manutencao = manutencao(StatusManutencao.ABERTA);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(manutencaoRepository.findById(2L)).thenReturn(Optional.of(manutencao));

        assertThrows(EstoqueInsuficienteException.class,
                () -> service.registrarSaida(1L, 2L, 3, "Saída"));
    }

    @Test
    void registrarSaidaDeveDecrementarPecaESalvarMovimentacao() {
        Peca peca = peca(5);
        peca.setCustoUnitario(new BigDecimal("12.50"));
        Manutencao manutencao = manutencao(StatusManutencao.ABERTA);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(manutencaoRepository.findById(2L)).thenReturn(Optional.of(manutencao));
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoEstoque resultado = service.registrarSaida(1L, 2L, 2, "Uso na manutenção");

        assertNotNull(resultado);
        assertEquals(3, peca.getQuantidadeAtual());
        assertEquals(peca, resultado.getPeca());
        assertEquals(TipoMovimentacao.SAIDA, resultado.getTipo());
        assertEquals(2, resultado.getQuantidade());
        assertEquals(manutencao, resultado.getManutencao());
        assertEquals("Uso na manutenção", resultado.getObservacao());
        assertEquals(new BigDecimal("12.50"), resultado.getCustoUnitarioMomento());
        verify(pecaRepository).save(peca);
        verify(movimentacaoEstoqueRepository).save(resultado);
    }

    @Test
    void registrarEntradaDeveLancarExcecaoQuandoPecaNaoExistir() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.registrarEntrada(1L, 2, "Entrada"));
    }

    @Test
    void registrarEntradaDeveIncrementarQuantidadeDaPeca() {
        Peca peca = peca(3);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoEstoque resultado = service.registrarEntrada(1L, 4, "Reposição");

        assertNotNull(resultado);
        assertEquals(7, peca.getQuantidadeAtual());
        assertEquals(TipoMovimentacao.ENTRADA, resultado.getTipo());
        assertEquals(4, resultado.getQuantidade());
        verify(pecaRepository).save(peca);
    }

    private Peca peca(int quantidadeAtual) {
        Peca peca = new Peca();
        peca.setId(1L);
        peca.setQuantidadeAtual(quantidadeAtual);
        return peca;
    }

    private Manutencao manutencao(StatusManutencao status) {
        Manutencao manutencao = new Manutencao();
        manutencao.setStatus(status);
        return manutencao;
    }
}
