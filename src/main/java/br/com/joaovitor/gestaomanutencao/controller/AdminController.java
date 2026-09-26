package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import br.com.joaovitor.gestaomanutencao.repository.SetorRepository;
import br.com.joaovitor.gestaomanutencao.service.MovimentacaoEstoqueService;
import br.com.joaovitor.gestaomanutencao.service.SolicitacaoCompraService;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EntityManager entityManager;
    private final SetorRepository setorRepository;
    private final MaquinaRepository maquinaRepository;
    private final PecaRepository pecaRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final SolicitacaoCompraService solicitacaoCompraService;

    public AdminController(
            EntityManager entityManager,
            SetorRepository setorRepository,
            MaquinaRepository maquinaRepository,
            PecaRepository pecaRepository,
            ManutencaoRepository manutencaoRepository,
            MovimentacaoEstoqueService movimentacaoEstoqueService,
            SolicitacaoCompraService solicitacaoCompraService
    ) {
        this.entityManager = entityManager;
        this.setorRepository = setorRepository;
        this.maquinaRepository = maquinaRepository;
        this.pecaRepository = pecaRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
        this.solicitacaoCompraService = solicitacaoCompraService;
    }

    @PostMapping("/reset-demo")
    @Transactional
    public ResponseEntity<Map<String, String>> resetarDadosDemo() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE movimentacao_estoque, solicitacao_compra, manutencao, peca, maquina, setor, orcamento_mensal "
                        + "RESTART IDENTITY CASCADE"
        ).executeUpdate();

        Setor setorCorteDobra = criarSetor("Corte e Dobra");
        Setor setorSolda = criarSetor("Solda");

        Maquina maq001 = criarMaquina("MAQ-001", "Torno CNC Romi para usinagem de pecas metalicas", setorCorteDobra);
        Maquina maq002 = criarMaquina("MAQ-002", "Prensa Hidraulica 200T", setorCorteDobra);
        Maquina maq003 = criarMaquina("MAQ-003", "Solda MIG Automatica", setorSolda);

        Peca pec001 = criarPeca("PEC-001", "Rolamento 6205", "Rolamentos", "un", "Prateleira A1", 15, 5, "45.90");
        Peca pec002 = criarPeca("PEC-002", "Correia Dentada", "Transmissao", "un", "Prateleira B2", 2, 5, "89.50");
        criarPeca("PEC-003", "Oleo Hidraulico ISO 68", "Lubrificantes", "L", "Deposito C", 40, 20, "18.30");
        Peca pec004 = criarPeca("PEC-004", "Eletrodo de Solda", "Consumiveis", "kg", "Prateleira D1", 3, 10, "25.00");

        Manutencao manutencao1 = criarManutencao(
                maq001, "Ruido anormal no rolamento do eixo principal", TipoManutencao.CORRETIVA, "Carlos Silva"
        );
        Manutencao manutencao2 = criarManutencao(
                maq002, "Troca de oleo hidraulico programada", TipoManutencao.PREVENTIVA, "Ana Torres"
        );
        Manutencao manutencao3 = criarManutencao(
                maq003, "Eletrodo insuficiente para concluir lote de producao", TipoManutencao.CORRETIVA, "Carlos Silva"
        );

        movimentacaoEstoqueService.registrarSaida(
                pec001.getId(), manutencao1.getId(), 2, "Substituicao do rolamento danificado"
        );

        concluirManutencao(
                manutencao1, "Substituido rolamento desgastado, lubrificacao geral do eixo", "180.00"
        );
        concluirManutencao(
                manutencao2, "Oleo hidraulico trocado conforme cronograma", "90.00"
        );

        solicitacaoCompraService.criar(pec002.getId(), null, 10, "Distribuidora Industrial SP", null);
        solicitacaoCompraService.criar(pec004.getId(), manutencao3.getId(), 15, "Soldas e Cia", null);

        return ResponseEntity.ok(Map.of("mensagem", "Dados de demonstracao resetados com sucesso."));
    }

    private Setor criarSetor(String nome) {
        Setor setor = new Setor();
        setor.setNome(nome);
        return setorRepository.save(setor);
    }

    private Maquina criarMaquina(String codigo, String descricao, Setor setor) {
        Maquina maquina = new Maquina();
        maquina.setCodigo(codigo);
        maquina.setDescricao(descricao);
        maquina.setSetor(setor);
        return maquinaRepository.save(maquina);
    }

    private Peca criarPeca(
            String codigo,
            String nome,
            String categoria,
            String unidadeMedida,
            String localizacaoFisica,
            int quantidadeAtual,
            int estoqueMinimo,
            String custoUnitario
    ) {
        Peca peca = new Peca();
        peca.setCodigo(codigo);
        peca.setNome(nome);
        peca.setCategoria(categoria);
        peca.setUnidadeMedida(unidadeMedida);
        peca.setLocalizacaoFisica(localizacaoFisica);
        peca.setQuantidadeAtual(quantidadeAtual);
        peca.setEstoqueMinimo(estoqueMinimo);
        peca.setCustoUnitario(new BigDecimal(custoUnitario));
        return pecaRepository.save(peca);
    }

    private Manutencao criarManutencao(
            Maquina maquina, String problemaDescricao, TipoManutencao tipo, String tecnicoResponsavel
    ) {
        Manutencao manutencao = new Manutencao();
        manutencao.setMaquina(maquina);
        manutencao.setProblemaDescricao(problemaDescricao);
        manutencao.setTipo(tipo);
        manutencao.setTecnicoResponsavel(tecnicoResponsavel);
        return manutencaoRepository.save(manutencao);
    }

    private void concluirManutencao(Manutencao manutencao, String descricaoServico, String custoMaoDeObra) {
        manutencao.setDescricaoServico(descricaoServico);
        manutencao.setCustoMaoDeObra(new BigDecimal(custoMaoDeObra));
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDataConclusao(LocalDateTime.now());
        manutencaoRepository.save(manutencao);
    }
}
