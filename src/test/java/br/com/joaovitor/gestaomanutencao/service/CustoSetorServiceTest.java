package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetorDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetoresResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.RelatorioParametrosInvalidosException;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.repository.SetorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustoSetorServiceTest {

    @Mock
    private SetorRepository setorRepository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private CustoManutencaoService custoManutencaoService;

    private CustoSetorService service;

    private Setor setorA;
    private Setor setorB;
    private Maquina maquina1;
    private Maquina maquina2;
    private Maquina maquinaSemSetor;

    private void configurarService() {
        service = new CustoSetorService(
                setorRepository, maquinaRepository, movimentacaoEstoqueRepository, custoManutencaoService
        );
    }

    private Setor setor(Long id, String nome, boolean ativo) {
        Setor setor = new Setor();
        setor.setId(id);
        setor.setNome(nome);
        setor.setAtivo(ativo);
        return setor;
    }

    private Maquina maquina(Long id, Setor setor) {
        Maquina maquina = new Maquina();
        maquina.setId(id);
        maquina.setCodigo("MAQ-" + id);
        maquina.setDescricao("Descrição " + id);
        maquina.setSetor(setor);
        maquina.setStatus(StatusMaquina.ATIVA);
        maquina.setCriadaEm(LocalDateTime.now());
        maquina.setAtualizadaEm(LocalDateTime.now());
        return maquina;
    }

    private void stubCustoTotalPorMaquina(Long maquinaId, BigDecimal pecas, BigDecimal maoDeObra) {
        lenient().when(movimentacaoEstoqueRepository.calcularCustoPecasPorMaquinaTotal(maquinaId)).thenReturn(pecas);
        lenient().when(custoManutencaoService.calcularCustoMaoDeObraPorMaquinaTotal(maquinaId)).thenReturn(maoDeObra);
    }

    @Test
    void somaDosCustosDosSetoresDeveSerIgualAoTotalGeral() {
        configurarService();
        setorA = setor(1L, "Produção", true);
        setorB = setor(2L, "Manutenção", true);
        maquina1 = maquina(10L, setorA);
        maquina2 = maquina(20L, setorB);

        when(setorRepository.findAll()).thenReturn(List.of(setorA, setorB));
        when(maquinaRepository.findAll()).thenReturn(List.of(maquina1, maquina2));
        stubCustoTotalPorMaquina(10L, new BigDecimal("100.00"), new BigDecimal("50.00"));
        stubCustoTotalPorMaquina(20L, new BigDecimal("30.00"), new BigDecimal("20.00"));

        RelatorioCustoSetoresResponseDTO resultado = service.calcularRelatorioCustoSetores(null, null, null);

        BigDecimal somaSetores = resultado.setores().stream()
                .map(RelatorioCustoSetorDTO::custoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(resultado.totalGeral(), somaSetores);
        assertEquals(new BigDecimal("200.00"), resultado.totalGeral());
    }

    @Test
    void maquinaSemSetorViraGrupoSemSetorApenasQuandoSetorIdsNaoInformado() {
        configurarService();
        setorA = setor(1L, "Produção", true);
        maquina1 = maquina(10L, setorA);
        maquinaSemSetor = maquina(30L, null);

        when(setorRepository.findAll()).thenReturn(List.of(setorA));
        when(maquinaRepository.findAll()).thenReturn(List.of(maquina1, maquinaSemSetor));
        stubCustoTotalPorMaquina(10L, new BigDecimal("100.00"), BigDecimal.ZERO);
        stubCustoTotalPorMaquina(30L, new BigDecimal("40.00"), BigDecimal.ZERO);

        RelatorioCustoSetoresResponseDTO resultado = service.calcularRelatorioCustoSetores(null, null, null);

        assertEquals(2, resultado.setores().size());
        RelatorioCustoSetorDTO ultimo = resultado.setores().get(resultado.setores().size() - 1);
        assertEquals("Sem setor", ultimo.setorNome());
        assertEquals(1, ultimo.quantidadeMaquinas());

        // Quando setorIds é informado, o grupo "Sem setor" não deve entrar
        when(setorRepository.findById(1L)).thenReturn(Optional.of(setorA));
        when(maquinaRepository.findBySetorIdIn(anyList())).thenReturn(List.of(maquina1));

        RelatorioCustoSetoresResponseDTO resultadoFiltrado =
                service.calcularRelatorioCustoSetores(List.of(1L), null, null);

        assertTrue(resultadoFiltrado.setores().stream().noneMatch(s -> "Sem setor".equals(s.setorNome())));
        assertEquals(1, resultadoFiltrado.setores().size());
    }

    @Test
    void setorInativoApareceNoRelatorio() {
        configurarService();
        Setor setorInativo = setor(3L, "Depósito", false);

        when(setorRepository.findAll()).thenReturn(List.of(setorInativo));
        when(maquinaRepository.findAll()).thenReturn(List.of());

        RelatorioCustoSetoresResponseDTO resultado = service.calcularRelatorioCustoSetores(null, null, null);

        assertEquals(1, resultado.setores().size());
        assertEquals(false, resultado.setores().get(0).ativo());
    }

    @Test
    void setorSemMaquinaApareceComZero() {
        configurarService();
        setorA = setor(1L, "Produção", true);

        when(setorRepository.findAll()).thenReturn(List.of(setorA));
        when(maquinaRepository.findAll()).thenReturn(List.of());

        RelatorioCustoSetoresResponseDTO resultado = service.calcularRelatorioCustoSetores(null, null, null);

        RelatorioCustoSetorDTO dto = resultado.setores().get(0);
        assertEquals(0, dto.quantidadeMaquinas());
        assertEquals(new BigDecimal("0.00"), dto.custoTotal());
    }

    @Test
    void setorIdsFiltraEPercentualFechaEm100SobreSelecionados() {
        configurarService();
        setorA = setor(1L, "Produção", true);
        setorB = setor(2L, "Manutenção", true);
        maquina1 = maquina(10L, setorA);
        maquina2 = maquina(20L, setorB);

        when(setorRepository.findById(1L)).thenReturn(Optional.of(setorA));
        when(setorRepository.findById(2L)).thenReturn(Optional.of(setorB));
        when(maquinaRepository.findBySetorIdIn(anyList())).thenReturn(List.of(maquina1, maquina2));
        stubCustoTotalPorMaquina(10L, new BigDecimal("75.00"), BigDecimal.ZERO);
        stubCustoTotalPorMaquina(20L, new BigDecimal("25.00"), BigDecimal.ZERO);

        RelatorioCustoSetoresResponseDTO resultado =
                service.calcularRelatorioCustoSetores(List.of(1L, 2L), null, null);

        BigDecimal somaPercentuais = resultado.setores().stream()
                .map(RelatorioCustoSetorDTO::percentualDoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("100.00"), somaPercentuais);
    }

    @Test
    void percentualComTotalGeralZeroDeveSerZeroSemDivisaoPorZero() {
        configurarService();
        setorA = setor(1L, "Produção", true);

        when(setorRepository.findAll()).thenReturn(List.of(setorA));
        when(maquinaRepository.findAll()).thenReturn(List.of());

        RelatorioCustoSetoresResponseDTO resultado = service.calcularRelatorioCustoSetores(null, null, null);

        assertEquals(new BigDecimal("0.00"), resultado.setores().get(0).percentualDoTotal());
        assertEquals(new BigDecimal("0.00"), resultado.totalGeral());
    }

    @Test
    void mesSemAnoDeveLancarRelatorioParametrosInvalidosException() {
        configurarService();
        assertThrows(RelatorioParametrosInvalidosException.class,
                () -> service.calcularRelatorioCustoSetores(null, 5, null));
    }

    @Test
    void mesForaDoIntervaloDeveLancarRelatorioParametrosInvalidosException() {
        configurarService();
        assertThrows(RelatorioParametrosInvalidosException.class,
                () -> service.calcularRelatorioCustoSetores(null, 13, 2026));
    }

    @Test
    void setorInexistenteDeveLancarRecursoNaoEncontradoException() {
        configurarService();
        when(setorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.calcularRelatorioCustoSetores(List.of(99L), null, null));
    }
}
