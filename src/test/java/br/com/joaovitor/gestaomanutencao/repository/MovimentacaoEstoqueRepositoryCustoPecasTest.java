package br.com.joaovitor.gestaomanutencao.repository;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.MovimentacaoEstoque;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoMovimentacao;
import br.com.joaovitor.gestaomanutencao.model.UnidadeMedida;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes de integração das queries Q1-Q4 de custo de peças (executam contra o
 * banco Postgres real configurado em application.properties, já que não há
 * banco embarcado no projeto). Cada cenário usa um ano fora do uso comum
 * (2099) e/ou uma máquina recém-criada para não colidir com dados já
 * existentes no banco.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "app.jwt.secret=chave-secreta-apenas-para-testes-automatizados-nao-use-em-producao",
        "spring.jpa.hibernate.ddl-auto=update"
})
class MovimentacaoEstoqueRepositoryCustoPecasTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Test
    void custoLiquidoConsideraDevolucaoParcialNoMesDeConclusao() {
        Manutencao manutencao = criarManutencao(
                StatusManutencao.CONCLUIDA,
                LocalDateTime.of(2099, 3, 10, 14, 0)
        );
        Peca peca = criarPeca();

        registrarMovimentacao(peca, manutencao, TipoMovimentacao.SAIDA, 5, new BigDecimal("10.00"));
        registrarMovimentacao(peca, manutencao, TipoMovimentacao.DEVOLUCAO, 2, new BigDecimal("10.00"));
        entityManager.flush();

        BigDecimal custo = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(3, 2099);

        assertEquals(0, new BigDecimal("30.00").compareTo(custo));
    }

    @Test
    void manutencaoCanceladaComDevolucaoTotalNaoEntraNaSoma() {
        Manutencao manutencao = criarManutencao(StatusManutencao.CANCELADA, null);
        Peca peca = criarPeca();

        registrarMovimentacao(peca, manutencao, TipoMovimentacao.SAIDA, 3, new BigDecimal("20.00"));
        registrarMovimentacao(peca, manutencao, TipoMovimentacao.DEVOLUCAO, 3, new BigDecimal("20.00"));
        entityManager.flush();

        BigDecimal custo = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(4, 2099);

        assertEquals(0, BigDecimal.ZERO.compareTo(custo));
    }

    @Test
    void manutencaoAbertaOuEmAndamentoNaoContribuiParaCustoAteConcluir() {
        Manutencao manutencao = criarManutencao(StatusManutencao.EM_ANDAMENTO, null);
        Peca peca = criarPeca();

        registrarMovimentacao(peca, manutencao, TipoMovimentacao.SAIDA, 4, new BigDecimal("15.00"));
        entityManager.flush();

        BigDecimal custo = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(5, 2099);

        assertEquals(0, BigDecimal.ZERO.compareTo(custo));
    }

    @Test
    void somaEZeroQuandoNaoHaNenhumaMovimentacaoParaAMaquina() {
        Maquina maquina = criarMaquina();
        entityManager.flush();

        BigDecimal custo = movimentacaoEstoqueRepository.calcularCustoPecasPorMaquinaTotal(maquina.getId());

        assertEquals(0, BigDecimal.ZERO.compareTo(custo));
    }

    private Manutencao criarManutencao(StatusManutencao status, LocalDateTime dataConclusao) {
        Maquina maquina = criarMaquina();

        Manutencao manutencao = new Manutencao();
        manutencao.setMaquina(maquina);
        manutencao.setProblemaDescricao("Falha simulada em teste");
        manutencao.setTipo(TipoManutencao.CORRETIVA);
        manutencao.setStatus(status);
        manutencao.setDataConclusao(dataConclusao);
        return entityManager.persist(manutencao);
    }

    private Maquina criarMaquina() {
        Setor setor = new Setor();
        setor.setNome("Setor Teste " + System.nanoTime());
        setor.setAtivo(true);
        entityManager.persist(setor);

        Maquina maquina = new Maquina();
        maquina.setCodigo("MAQ-TESTE-" + System.nanoTime());
        maquina.setDescricao("Máquina de teste");
        maquina.setSetor(setor);
        maquina.setStatus(StatusMaquina.ATIVA);
        return entityManager.persist(maquina);
    }

    private Peca criarPeca() {
        Peca peca = new Peca();
        peca.setCodigo("PEC-TESTE-" + System.nanoTime());
        peca.setNome("Peça de teste");
        peca.setUnidadeMedida(UnidadeMedida.UN);
        peca.setQuantidadeAtual(100);
        peca.setEstoqueMinimo(1);
        peca.setCustoUnitario(new BigDecimal("10.00"));
        return entityManager.persist(peca);
    }

    private void registrarMovimentacao(
            Peca peca, Manutencao manutencao, TipoMovimentacao tipo, Integer quantidade, BigDecimal custoUnitario
    ) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setPeca(peca);
        movimentacao.setManutencao(manutencao);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setCustoUnitarioMomento(custoUnitario);
        movimentacao.setObservacao("Movimentação de teste");
        entityManager.persist(movimentacao);
    }
}
