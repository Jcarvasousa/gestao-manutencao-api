package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.ServicoTerceiroValorInvalidoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoTerceiroServiceTest {

    @Mock
    private ServicoTerceiroRepository servicoTerceiroRepository;

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @InjectMocks
    private ServicoTerceiroService service;

    @Test
    void criarNoModoHorasDeveCalcularValorApuradoComEscalaDoisEHalfUp() {
        when(manutencaoRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(manutencao(StatusManutencao.ABERTA)));

        ServicoTerceiro resultado = service.criar(
                1L, null, new BigDecimal("3.5"), new BigDecimal("33.333"), "Retífica", "Fornecedor"
        );

        // 3.50 x 33.33 = 116.655 -> 116.66 (HALF_UP); entradas normalizadas para 2 casas
        assertEquals(new BigDecimal("116.66"), resultado.getValorApurado());
        assertEquals(new BigDecimal("3.50"), resultado.getHorasTrabalhadas());
        assertEquals(new BigDecimal("33.33"), resultado.getValorHora());
        assertNull(resultado.getValorFinal());
        verify(servicoTerceiroRepository).save(resultado);
    }

    @Test
    void criarNoModoDiretoDeveManterValorApurado() {
        when(manutencaoRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(manutencao(StatusManutencao.EM_ANDAMENTO)));

        ServicoTerceiro resultado = service.criar(1L, new BigDecimal("450.00"), null, null, "Solda", null);

        assertEquals(new BigDecimal("450.00"), resultado.getValorApurado());
        assertNull(resultado.getHorasTrabalhadas());
        assertNull(resultado.getValorHora());
    }

    @Test
    void criarDeveRejeitarOsDoisModosJuntos() {
        assertThrows(ServicoTerceiroValorInvalidoException.class, () -> service.criar(
                1L, new BigDecimal("100"), new BigDecimal("2"), new BigDecimal("50"), "x", null
        ));
        verify(servicoTerceiroRepository, never()).save(any(ServicoTerceiro.class));
    }

    @Test
    void criarDeveRejeitarApenasHorasOuApenasValorHora() {
        assertThrows(ServicoTerceiroValorInvalidoException.class, () -> service.criar(
                1L, null, new BigDecimal("2"), null, "x", null
        ));
        assertThrows(ServicoTerceiroValorInvalidoException.class, () -> service.criar(
                1L, null, null, new BigDecimal("50"), "x", null
        ));
        assertThrows(ServicoTerceiroValorInvalidoException.class, () -> service.criar(
                1L, new BigDecimal("100"), new BigDecimal("2"), null, "x", null
        ));
    }

    @Test
    void criarDeveRejeitarQuandoNenhumValorForInformado() {
        assertThrows(ServicoTerceiroValorInvalidoException.class, () -> service.criar(1L, null, null, null, "x", null));
    }

    @Test
    void criarDeveLancarExcecaoQuandoManutencaoEstiverFinalizada() {
        when(manutencaoRepository.buscarPorIdComTrava(1L)).thenReturn(Optional.of(manutencao(StatusManutencao.CONCLUIDA)));

        assertThrows(ManutencaoNaoEstaAbertaException.class,
                () -> service.criar(1L, new BigDecimal("100"), null, null, "x", null));
        verify(servicoTerceiroRepository, never()).save(any(ServicoTerceiro.class));
    }

    @Test
    void atualizarDeveAlterarSomenteValorFinalEObservacaoEmManutencaoConcluida() {
        ServicoTerceiro servico = servico(StatusManutencao.CONCLUIDA);
        when(servicoTerceiroRepository.findById(5L)).thenReturn(Optional.of(servico));

        ServicoTerceiro resultado = service.atualizar(5L, new BigDecimal("95.00"), "Desconto");

        assertEquals(new BigDecimal("95.00"), resultado.getValorFinal());
        assertEquals("Desconto", resultado.getObservacao());
        assertEquals(new BigDecimal("100.00"), resultado.getValorApurado());
        assertEquals("Solda", resultado.getDescricao());
    }

    @Test
    void atualizarComValorFinalNuloDeveLimparValorFinal() {
        ServicoTerceiro servico = servico(StatusManutencao.EM_ANDAMENTO);
        servico.setValorFinal(new BigDecimal("95.00"));
        when(servicoTerceiroRepository.findById(5L)).thenReturn(Optional.of(servico));

        assertNull(service.atualizar(5L, null, null).getValorFinal());
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoManutencaoEstiverCancelada() {
        when(servicoTerceiroRepository.findById(5L)).thenReturn(Optional.of(servico(StatusManutencao.CANCELADA)));

        assertThrows(ManutencaoNaoEstaAbertaException.class, () -> service.atualizar(5L, BigDecimal.ONE, "x"));
    }

    @Test
    void excluirDeveLancarExcecaoQuandoManutencaoEstiverFinalizada() {
        ServicoTerceiro servico = servico(StatusManutencao.CONCLUIDA);
        when(servicoTerceiroRepository.findById(5L)).thenReturn(Optional.of(servico));

        assertThrows(ManutencaoNaoEstaAbertaException.class, () -> service.excluir(5L));
        verify(servicoTerceiroRepository, never()).delete(any(ServicoTerceiro.class));
    }

    @Test
    void excluirDeveRemoverServicoDeManutencaoAberta() {
        ServicoTerceiro servico = servico(StatusManutencao.ABERTA);
        when(servicoTerceiroRepository.findById(5L)).thenReturn(Optional.of(servico));

        service.excluir(5L);

        verify(servicoTerceiroRepository).delete(servico);
    }

    private ServicoTerceiro servico(StatusManutencao status) {
        ServicoTerceiro servico = new ServicoTerceiro();
        servico.setId(5L);
        servico.setManutencao(manutencao(status));
        servico.setValorApurado(new BigDecimal("100.00"));
        servico.setDescricao("Solda");
        return servico;
    }

    private Manutencao manutencao(StatusManutencao status) {
        Manutencao manutencao = new Manutencao();
        manutencao.setId(1L);
        manutencao.setStatus(status);
        return manutencao;
    }
}
