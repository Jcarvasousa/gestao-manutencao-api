package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;
import br.com.joaovitor.gestaomanutencao.model.OrcamentoMensal;
import br.com.joaovitor.gestaomanutencao.model.Peca;
import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.model.SolicitacaoCompra;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.model.StatusMaquina;
import br.com.joaovitor.gestaomanutencao.model.Tecnico;
import br.com.joaovitor.gestaomanutencao.model.TipoManutencao;
import br.com.joaovitor.gestaomanutencao.model.UnidadeMedida;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoTecnicoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.OrcamentoMensalRepository;
import br.com.joaovitor.gestaomanutencao.repository.PecaRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import br.com.joaovitor.gestaomanutencao.repository.SetorRepository;
import br.com.joaovitor.gestaomanutencao.repository.TecnicoRepository;
import br.com.joaovitor.gestaomanutencao.service.MovimentacaoEstoqueService;
import br.com.joaovitor.gestaomanutencao.service.SolicitacaoCompraService;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final long SEED = 42L;

    private final EntityManager entityManager;
    private final SetorRepository setorRepository;
    private final MaquinaRepository maquinaRepository;
    private final PecaRepository pecaRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ManutencaoTecnicoRepository manutencaoTecnicoRepository;
    private final ServicoTerceiroRepository servicoTerceiroRepository;
    private final OrcamentoMensalRepository orcamentoMensalRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;
    private final SolicitacaoCompraService solicitacaoCompraService;

    public AdminController(
            EntityManager entityManager,
            SetorRepository setorRepository,
            MaquinaRepository maquinaRepository,
            PecaRepository pecaRepository,
            ManutencaoRepository manutencaoRepository,
            TecnicoRepository tecnicoRepository,
            ManutencaoTecnicoRepository manutencaoTecnicoRepository,
            ServicoTerceiroRepository servicoTerceiroRepository,
            OrcamentoMensalRepository orcamentoMensalRepository,
            MovimentacaoEstoqueService movimentacaoEstoqueService,
            SolicitacaoCompraService solicitacaoCompraService
    ) {
        this.entityManager = entityManager;
        this.setorRepository = setorRepository;
        this.maquinaRepository = maquinaRepository;
        this.pecaRepository = pecaRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.manutencaoTecnicoRepository = manutencaoTecnicoRepository;
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.orcamentoMensalRepository = orcamentoMensalRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
        this.solicitacaoCompraService = solicitacaoCompraService;
    }

    @PostMapping("/reset-demo")
    @Transactional
    public ResponseEntity<Map<String, String>> resetarDadosDemo() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE movimentacao_estoque, solicitacao_compra, manutencao_tecnico, servico_terceiro, "
                        + "tecnico, manutencao, peca, maquina, setor, orcamento_mensal "
                        + "RESTART IDENTITY CASCADE"
        ).executeUpdate();

        Random random = new Random(SEED);

        List<Setor> setores = gerarSetores();
        List<Maquina> maquinas = gerarMaquinas(setores);
        List<Peca> pecas = gerarPecas();
        List<Tecnico> tecnicos = gerarTecnicos();

        gerarOrcamentosMensais(random);

        List<Peca> pecasParaMovimentacao = pecas.stream()
                .filter(peca -> peca.getQuantidadeAtual() >= peca.getEstoqueMinimo())
                .toList();
        Map<Long, Integer> estoqueSimulado = new HashMap<>();
        for (Peca peca : pecasParaMovimentacao) {
            estoqueSimulado.put(peca.getId(), peca.getQuantidadeAtual());
        }

        gerarMovimentacoesEntradaAvulsas(pecasParaMovimentacao, random);
        gerarManutencoes(maquinas, pecasParaMovimentacao, estoqueSimulado, tecnicos, random);
        gerarComprasRecebidasHistoricas(pecasParaMovimentacao);
        gerarSolicitacoesCompraParaEstoqueBaixo(pecas);

        return ResponseEntity.ok(Map.of("mensagem", "Dados de demonstracao resetados com sucesso."));
    }

    // ------------------------------------------------------------------
    // Setores e Maquinas
    // ------------------------------------------------------------------

    private List<Setor> gerarSetores() {
        List<Setor> setores = new ArrayList<>();
        setores.add(criarSetor("Corte e Dobra"));
        setores.add(criarSetor("Solda"));
        setores.add(criarSetor("Usinagem"));
        setores.add(criarSetor("Pintura"));
        setores.add(criarSetor("Montagem"));
        return setores;
    }

    private record MaquinaSeed(String descricao, int setorIndex, StatusMaquina status) {
    }

    private List<Maquina> gerarMaquinas(List<Setor> setores) {
        List<MaquinaSeed> seeds = List.of(
                // Corte e Dobra (setorIndex 0)
                new MaquinaSeed("Guilhotina Hidraulica para corte de chapas de aco", 0, StatusMaquina.ATIVA),
                new MaquinaSeed("Prensa Dobradeira CNC 3000mm", 0, StatusMaquina.ATIVA),
                new MaquinaSeed("Prensa Hidraulica 200T", 0, StatusMaquina.ATIVA),
                new MaquinaSeed("Maquina de Corte a Plasma CNC", 0, StatusMaquina.PARADA),
                new MaquinaSeed("Serra Fita Automatica para perfis metalicos", 0, StatusMaquina.ATIVA),
                // Solda (setorIndex 1)
                new MaquinaSeed("Solda MIG Automatica", 1, StatusMaquina.ATIVA),
                new MaquinaSeed("Solda TIG para aco inoxidavel", 1, StatusMaquina.ATIVA),
                new MaquinaSeed("Robo de Solda a Ponto", 1, StatusMaquina.EM_MANUTENCAO),
                new MaquinaSeed("Maquina de Solda por Resistencia", 1, StatusMaquina.ATIVA),
                new MaquinaSeed("Cabine de Solda com Exaustao", 1, StatusMaquina.ATIVA),
                // Usinagem (setorIndex 2)
                new MaquinaSeed("Torno CNC Romi para usinagem de pecas metalicas", 2, StatusMaquina.ATIVA),
                new MaquinaSeed("Fresadora CNC Vertical", 2, StatusMaquina.ATIVA),
                new MaquinaSeed("Centro de Usinagem 5 Eixos", 2, StatusMaquina.ATIVA),
                new MaquinaSeed("Retifica Cilindrica de Precisao", 2, StatusMaquina.PARADA),
                new MaquinaSeed("Furadeira de Bancada Industrial", 2, StatusMaquina.ATIVA),
                // Pintura (setorIndex 3)
                new MaquinaSeed("Cabine de Pintura Eletrostatica", 3, StatusMaquina.ATIVA),
                new MaquinaSeed("Estufa de Secagem para Pintura", 3, StatusMaquina.ATIVA),
                new MaquinaSeed("Linha de Pintura por Imersao (E-coat)", 3, StatusMaquina.EM_MANUTENCAO),
                new MaquinaSeed("Pistola de Pintura Automatica CNC", 3, StatusMaquina.ATIVA),
                new MaquinaSeed("Sistema de Jateamento de Granalha", 3, StatusMaquina.ATIVA),
                // Montagem (setorIndex 4)
                new MaquinaSeed("Linha de Montagem Automatizada", 4, StatusMaquina.ATIVA),
                new MaquinaSeed("Esteira Transportadora de Componentes", 4, StatusMaquina.ATIVA),
                new MaquinaSeed("Parafusadeira Pneumatica Automatica", 4, StatusMaquina.ATIVA),
                new MaquinaSeed("Bancada de Montagem com Torquimetro Digital", 4, StatusMaquina.PARADA),
                new MaquinaSeed("Maquina de Teste Funcional de Produtos", 4, StatusMaquina.ATIVA)
        );

        List<Maquina> maquinas = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            MaquinaSeed seed = seeds.get(i);
            String codigo = "MAQ-%03d".formatted(i + 1);
            maquinas.add(criarMaquina(codigo, seed.descricao(), setores.get(seed.setorIndex()), seed.status()));
        }
        return maquinas;
    }

    // ------------------------------------------------------------------
    // Pecas
    // ------------------------------------------------------------------

    private record PecaSeed(
            String nome, String categoria, UnidadeMedida unidadeMedida, String localizacaoFisica,
            int quantidadeAtual, int estoqueMinimo, String custoUnitario
    ) {
    }

    private List<Peca> gerarPecas() {
        List<PecaSeed> seeds = List.of(
                new PecaSeed("Rolamento 6205", "Rolamentos", UnidadeMedida.UN, "Prateleira A1", 45, 10, "45.90"),
                new PecaSeed("Rolamento 6206", "Rolamentos", UnidadeMedida.UN, "Prateleira A1", 3, 10, "52.30"),
                new PecaSeed("Correia Dentada A47", "Transmissao", UnidadeMedida.UN, "Prateleira B2", 22, 5, "89.50"),
                new PecaSeed("Correia em V B52", "Transmissao", UnidadeMedida.UN, "Prateleira B2", 18, 5, "76.20"),
                new PecaSeed("Oleo Hidraulico ISO 68", "Lubrificantes", UnidadeMedida.L, "Deposito C", 120, 40, "18.30"),
                new PecaSeed("Oleo Hidraulico ISO 32", "Lubrificantes", UnidadeMedida.L, "Deposito C", 95, 30, "17.10"),
                new PecaSeed("Eletrodo de Solda E6013", "Consumiveis de Solda", UnidadeMedida.KG, "Prateleira D1", 60, 15, "25.00"),
                new PecaSeed("Arame de Solda MIG", "Consumiveis de Solda", UnidadeMedida.KG, "Prateleira D1", 48, 15, "32.40"),
                new PecaSeed("Eletrodo de Solda E7018", "Consumiveis de Solda", UnidadeMedida.CAIXA, "Prateleira D2", 25, 8, "68.90"),
                new PecaSeed("Disjuntor Bipolar 32A", "Eletrica", UnidadeMedida.UN, "Armario E1", 2, 8, "34.50"),
                new PecaSeed("Contator Tripolar 25A", "Eletrica", UnidadeMedida.UN, "Armario E1", 14, 6, "58.70"),
                new PecaSeed("Sensor Indutivo M12", "Eletrica", UnidadeMedida.UN, "Armario E1", 20, 6, "42.00"),
                new PecaSeed("Valvula Solenoide 24V", "Pneumatica", UnidadeMedida.UN, "Prateleira F1", 16, 5, "95.00"),
                new PecaSeed("Cilindro Pneumatico 100mm", "Pneumatica", UnidadeMedida.UN, "Prateleira F1", 12, 4, "210.00"),
                new PecaSeed("Filtro de Ar Comprimido", "Pneumatica", UnidadeMedida.UN, "Prateleira F2", 30, 10, "27.80"),
                new PecaSeed("Graxa Industrial Multiuso", "Lubrificantes", UnidadeMedida.KG, "Deposito C", 55, 15, "22.60"),
                new PecaSeed("Parafuso Sextavado M8x30", "Fixadores", UnidadeMedida.CAIXA, "Prateleira G1", 40, 10, "35.00"),
                new PecaSeed("Parafuso Sextavado M10x40", "Fixadores", UnidadeMedida.CAIXA, "Prateleira G1", 32, 10, "42.00"),
                new PecaSeed("Porca Sextavada M8", "Fixadores", UnidadeMedida.CAIXA, "Prateleira G2", 4, 10, "18.00"),
                new PecaSeed("Arruela de Pressao M8", "Fixadores", UnidadeMedida.PACOTE, "Prateleira G2", 28, 8, "12.50"),
                new PecaSeed("Retentor de Oleo 30x47x7", "Vedacao", UnidadeMedida.UN, "Prateleira H1", 24, 8, "15.90"),
                new PecaSeed("Retentor de Oleo 40x62x8", "Vedacao", UnidadeMedida.UN, "Prateleira H1", 19, 8, "17.40"),
                new PecaSeed("Anel O-ring Viton 20mm", "Vedacao", UnidadeMedida.UN, "Prateleira H2", 50, 15, "3.80"),
                new PecaSeed("Massa Epoxi para Vedacao", "Vedacao", UnidadeMedida.SACO, "Prateleira H2", 3, 6, "48.00"),
                new PecaSeed("Mangueira Hidraulica 1/2 polegada", "Hidraulica", UnidadeMedida.M, "Deposito C", 80, 20, "14.20"),
                new PecaSeed("Cabo Eletrico Flexivel 2,5mm2", "Eletrica", UnidadeMedida.M, "Armario E2", 200, 50, "3.20"),
                new PecaSeed("Lona Abrasiva Grao 80", "Abrasivos", UnidadeMedida.M2, "Prateleira I1", 35, 10, "22.00"),
                new PecaSeed("Disco de Corte 7 polegadas", "Abrasivos", UnidadeMedida.CAIXA, "Prateleira I1", 18, 5, "62.00"),
                new PecaSeed("Disco de Desbaste 4.1/2 polegadas", "Abrasivos", UnidadeMedida.UN, "Prateleira I2", 60, 20, "8.50"),
                new PecaSeed("Tinta Esmalte Sintetico Cinza", "Tintas", UnidadeMedida.L, "Deposito Pintura", 40, 12, "55.00"),
                new PecaSeed("Tinta Primer Anticorrosivo", "Tintas", UnidadeMedida.L, "Deposito Pintura", 36, 12, "62.00"),
                new PecaSeed("Solvente Redutor", "Tintas", UnidadeMedida.ML, "Deposito Pintura", 5000, 1000, "0.08"),
                new PecaSeed("Bico de Pistola de Pintura", "Pintura", UnidadeMedida.UN, "Prateleira J1", 4, 6, "89.00"),
                new PecaSeed("Rolamento de Esfera 6304", "Rolamentos", UnidadeMedida.UN, "Prateleira A2", 28, 8, "61.50"),
                new PecaSeed("Chaveta Metalica 8x7x40", "Fixadores", UnidadeMedida.UN, "Prateleira G3", 45, 10, "6.80"),
                new PecaSeed("Estopa Industrial", "Consumiveis Gerais", UnidadeMedida.KG, "Deposito Geral", 25, 8, "14.00"),
                new PecaSeed("Fusivel NH Tamanho 00", "Eletrica", UnidadeMedida.UN, "Armario E2", 22, 8, "19.90"),
                new PecaSeed("Lampada Sinalizadora LED 24V", "Eletrica", UnidadeMedida.UN, "Armario E2", 2, 6, "11.30"),
                new PecaSeed("Correia Poly-V", "Transmissao", UnidadeMedida.UN, "Prateleira B3", 16, 5, "68.00"),
                new PecaSeed("Rolamento de Rolo Conico 30205", "Rolamentos", UnidadeMedida.UN, "Prateleira A2", 12, 4, "112.00")
        );

        List<Peca> pecas = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            PecaSeed seed = seeds.get(i);
            String codigo = "PEC-%03d".formatted(i + 1);
            pecas.add(criarPeca(
                    codigo, seed.nome(), seed.categoria(), seed.unidadeMedida(), seed.localizacaoFisica(),
                    seed.quantidadeAtual(), seed.estoqueMinimo(), seed.custoUnitario()
            ));
        }
        return pecas;
    }

    // ------------------------------------------------------------------
    // Tecnicos
    // ------------------------------------------------------------------

    private record TecnicoSeed(String nome, String salarioMensal, String cargaHorariaDiaria) {
    }

    private List<Tecnico> gerarTecnicos() {
        List<TecnicoSeed> seeds = List.of(
                new TecnicoSeed("Carlos Eduardo Santos", "3200.00", "8"),
                new TecnicoSeed("Roberto Lima Ferreira", "3450.00", "8"),
                new TecnicoSeed("Marcos Paulo Oliveira", "2980.00", "7"),
                new TecnicoSeed("Fernanda Souza Almeida", "3800.00", "7.5"),
                new TecnicoSeed("Juliana Ribeiro Costa", "4150.00", "8")
        );

        List<Tecnico> tecnicos = new ArrayList<>();
        for (TecnicoSeed seed : seeds) {
            tecnicos.add(criarTecnico(seed.nome(), seed.salarioMensal(), seed.cargaHorariaDiaria()));
        }
        return tecnicos;
    }

    // ------------------------------------------------------------------
    // Orcamento mensal
    // ------------------------------------------------------------------

    private void gerarOrcamentosMensais(Random random) {
        LocalDateTime agora = LocalDateTime.now();
        for (int i = 0; i < 12; i++) {
            LocalDateTime referencia = agora.minusMonths(i);
            BigDecimal valor = BigDecimal.valueOf(60000 + random.nextInt(20001));
            OrcamentoMensal orcamento = new OrcamentoMensal();
            orcamento.setMes(referencia.getMonthValue());
            orcamento.setAno(referencia.getYear());
            orcamento.setValorPlanejado(valor);
            orcamentoMensalRepository.save(orcamento);
        }
    }

    // ------------------------------------------------------------------
    // Movimentacoes de entrada avulsas
    // ------------------------------------------------------------------

    private void gerarMovimentacoesEntradaAvulsas(List<Peca> pecasDisponiveis, Random random) {
        int quantidadeEntradas = 10;
        for (int i = 0; i < quantidadeEntradas; i++) {
            Peca peca = pecasDisponiveis.get(random.nextInt(pecasDisponiveis.size()));
            int quantidade = 10 + random.nextInt(41);
            movimentacaoEstoqueService.registrarEntrada(
                    peca.getId(), quantidade, "Reposicao de estoque - compra programada"
            );
        }
    }

    // ------------------------------------------------------------------
    // Manutencoes
    // ------------------------------------------------------------------

    private static final List<String> PROBLEMAS_CORRETIVA = List.of(
            "Ruido anormal no rolamento do eixo principal",
            "Vazamento de oleo hidraulico identificado",
            "Maquina parou de funcionar durante operacao",
            "Falha no sistema eletrico de comando",
            "Superaquecimento do motor durante uso continuo",
            "Vibracao excessiva na estrutura da maquina",
            "Sensor de posicao apresentando leitura incorreta",
            "Correia de transmissao rompida",
            "Painel de controle travando intermitentemente",
            "Vazamento de ar no sistema pneumatico"
    );

    private static final List<String> PROBLEMAS_PREVENTIVA = List.of(
            "Troca de oleo hidraulico programada",
            "Lubrificacao geral dos componentes moveis",
            "Inspecao periodica do sistema eletrico",
            "Verificacao e calibracao de sensores",
            "Substituicao preventiva de correias e filtros",
            "Manutencao preventiva trimestral programada",
            "Limpeza e inspecao do sistema pneumatico",
            "Revisao geral conforme plano de manutencao"
    );

    private static final List<String> DESCRICOES_SERVICO = List.of(
            "Substituido componente desgastado, sistema testado e aprovado",
            "Realizada lubrificacao geral e ajuste dos componentes",
            "Corrigido vazamento e reposto nivel de fluido",
            "Peca substituida e maquina testada em vazio e em carga",
            "Reparo eletrico concluido, sistema de comando normalizado",
            "Servico concluido conforme cronograma preventivo",
            "Ajuste mecanico realizado, folga eliminada",
            "Componente recondicionado e reinstalado com sucesso"
    );

    private static final List<String> CONDICOES_SEGURANCA = List.of(
            "Maquina bloqueada e sinalizada (LOTO) durante o servico, EPIs utilizados conforme NR12",
            "Servico realizado com maquina desenergizada e sinalizacao de area, conforme NR12",
            "Procedimento de bloqueio e etiquetagem seguido integralmente, EPIs adequados utilizados",
            "Area isolada durante manutencao, dispositivos de seguranca testados apos o servico"
    );

    private static final List<String> FORNECEDORES_TERCEIRO = List.of(
            "Metal Service Manutencoes Ltda",
            "TecnoSolda Servicos Industriais",
            "ElexPro Manutencao Eletrica",
            "Hidraulica Forte Servicos",
            "Usimaq Retifica e Usinagem"
    );

    private static final List<String> DESCRICOES_TERCEIRO = List.of(
            "Servico especializado de retifica externa",
            "Calibracao e manutencao de painel eletrico",
            "Solda especializada em liga metalica",
            "Manutencao hidraulica externa com pecas proprias",
            "Servico terceirizado de balanceamento"
    );

    // distribuicao nao uniforme das 94 manutencoes concluidas pelos ultimos 12 meses (indice 0 = mes atual)
    private static final int[] DISTRIBUICAO_MESES = {7, 7, 7, 8, 9, 10, 11, 9, 8, 7, 6, 5};

    private void gerarManutencoes(
            List<Maquina> maquinas, List<Peca> pecasParaMovimentacao, Map<Long, Integer> estoqueSimulado,
            List<Tecnico> tecnicos, Random random
    ) {
        LocalDateTime agora = LocalDateTime.now();

        for (int mesesAtras = 0; mesesAtras < DISTRIBUICAO_MESES.length; mesesAtras++) {
            int quantidade = DISTRIBUICAO_MESES[mesesAtras];
            int minDiasAtras = mesesAtras == 0 ? 5 : 0;
            for (int i = 0; i < quantidade; i++) {
                LocalDateTime dataAbertura = agora.minusMonths(mesesAtras)
                        .minusDays(minDiasAtras + random.nextInt(23));
                gerarManutencaoConcluida(maquinas, pecasParaMovimentacao, estoqueSimulado, tecnicos, random, dataAbertura);
            }
        }

        gerarBacklogPendente(maquinas, random);
    }

    private void gerarManutencaoConcluida(
            List<Maquina> maquinas, List<Peca> pecasParaMovimentacao, Map<Long, Integer> estoqueSimulado,
            List<Tecnico> tecnicos, Random random, LocalDateTime dataAbertura
    ) {
        Maquina maquina = maquinas.get(random.nextInt(maquinas.size()));
        boolean corretiva = random.nextDouble() < 0.65;
        TipoManutencao tipo = corretiva ? TipoManutencao.CORRETIVA : TipoManutencao.PREVENTIVA;
        List<String> problemas = corretiva ? PROBLEMAS_CORRETIVA : PROBLEMAS_PREVENTIVA;
        String problema = problemas.get(random.nextInt(problemas.size()));

        Manutencao manutencao = criarManutencao(maquina, problema, tipo);
        definirDataAberturaHistorica(manutencao, dataAbertura);

        vincularResponsaveis(manutencao, tecnicos, random);

        double chancePeca = corretiva ? 0.65 : 0.15;
        if (random.nextDouble() < chancePeca) {
            registrarUsoDePecas(manutencao, pecasParaMovimentacao, estoqueSimulado, random);
        }

        LocalDateTime dataInicio = dataAbertura.plusHours(random.nextInt(48));
        int duracaoHoras = corretiva ? 2 + random.nextInt(71) : 4 + random.nextInt(45);
        LocalDateTime dataConclusao = dataInicio.plusHours(duracaoHoras);

        concluirManutencao(
                manutencao,
                DESCRICOES_SERVICO.get(random.nextInt(DESCRICOES_SERVICO.size())),
                CONDICOES_SEGURANCA.get(random.nextInt(CONDICOES_SEGURANCA.size())),
                random.nextDouble() < 0.9,
                dataInicio,
                dataConclusao
        );
    }

    private void vincularResponsaveis(Manutencao manutencao, List<Tecnico> tecnicos, Random random) {
        double r = random.nextDouble();
        if (r < 0.60) {
            vincularTecnico(manutencao, tecnicos.get(random.nextInt(tecnicos.size())), random);
        } else if (r < 0.80) {
            int quantidadeTecnicos = 2 + (random.nextBoolean() ? 1 : 0);
            Set<Integer> indicesEscolhidos = new HashSet<>();
            while (indicesEscolhidos.size() < Math.min(quantidadeTecnicos, tecnicos.size())) {
                indicesEscolhidos.add(random.nextInt(tecnicos.size()));
            }
            for (int indice : indicesEscolhidos) {
                vincularTecnico(manutencao, tecnicos.get(indice), random);
            }
        } else if (r < 0.95) {
            vincularServicoTerceiro(manutencao, random);
        } else {
            vincularTecnico(manutencao, tecnicos.get(random.nextInt(tecnicos.size())), random);
            vincularServicoTerceiro(manutencao, random);
        }
    }

    private void registrarUsoDePecas(
            Manutencao manutencao, List<Peca> pecasParaMovimentacao, Map<Long, Integer> estoqueSimulado, Random random
    ) {
        int numeroPecas = 1 + random.nextInt(3);
        Set<Long> pecasUsadasNestaManutencao = new HashSet<>();
        for (int i = 0; i < numeroPecas; i++) {
            Peca peca = pecasParaMovimentacao.get(random.nextInt(pecasParaMovimentacao.size()));
            if (!pecasUsadasNestaManutencao.add(peca.getId())) {
                continue;
            }
            int quantidade = 1 + random.nextInt(5);
            int estoqueAtual = estoqueSimulado.getOrDefault(peca.getId(), 0);
            if (estoqueAtual < quantidade) {
                continue;
            }
            movimentacaoEstoqueService.registrarSaida(
                    peca.getId(), manutencao.getId(), quantidade, "Peca utilizada durante o servico de manutencao"
            );
            estoqueSimulado.put(peca.getId(), estoqueAtual - quantidade);
        }
    }

    private void gerarBacklogPendente(List<Maquina> maquinas, Random random) {
        LocalDateTime agora = LocalDateTime.now();

        for (int i = 0; i < 4; i++) {
            Maquina maquina = maquinas.get(random.nextInt(maquinas.size()));
            boolean corretiva = random.nextDouble() < 0.65;
            TipoManutencao tipo = corretiva ? TipoManutencao.CORRETIVA : TipoManutencao.PREVENTIVA;
            List<String> problemas = corretiva ? PROBLEMAS_CORRETIVA : PROBLEMAS_PREVENTIVA;
            String problema = problemas.get(random.nextInt(problemas.size()));

            Manutencao manutencao = criarManutencao(maquina, problema, tipo);
            definirDataAberturaHistorica(manutencao, agora.minusDays(1 + random.nextInt(10)));
        }

        for (int i = 0; i < 2; i++) {
            Maquina maquina = maquinas.get(random.nextInt(maquinas.size()));
            String problema = PROBLEMAS_CORRETIVA.get(random.nextInt(PROBLEMAS_CORRETIVA.size()));

            Manutencao manutencao = criarManutencao(maquina, problema, TipoManutencao.CORRETIVA);
            LocalDateTime dataAbertura = agora.minusDays(1 + random.nextInt(10));
            definirDataAberturaHistorica(manutencao, dataAbertura);
            manutencao.setStatus(StatusManutencao.EM_ANDAMENTO);
            manutencao.setDataInicio(dataAbertura.plusHours(random.nextInt(24)));
            manutencaoRepository.save(manutencao);
        }
    }

    private void gerarSolicitacoesCompraParaEstoqueBaixo(List<Peca> pecas) {
        List<Peca> pecasAbaixoDoMinimo = pecas.stream()
                .filter(peca -> peca.getQuantidadeAtual() < peca.getEstoqueMinimo())
                .toList();
        for (int i = 0; i < pecasAbaixoDoMinimo.size(); i++) {
            Peca peca = pecasAbaixoDoMinimo.get(i);
            String fornecedor = FORNECEDORES_TERCEIRO.get(i % FORNECEDORES_TERCEIRO.size());
            int quantidadeNecessaria = (peca.getEstoqueMinimo() - peca.getQuantidadeAtual()) + peca.getEstoqueMinimo();
            solicitacaoCompraService.criar(peca.getId(), null, quantidadeNecessaria, fornecedor, null);
        }
    }

    // Random proprio (SEED + 1) para nao consumir a sequencia do Random(SEED) usada nos demais geradores
    private void gerarComprasRecebidasHistoricas(List<Peca> pecasParaMovimentacao) {
        Random random = new Random(SEED + 1);
        LocalDateTime agora = LocalDateTime.now();

        for (int mesesAtras = 0; mesesAtras < 12; mesesAtras++) {
            Peca peca = pecasParaMovimentacao.get(random.nextInt(pecasParaMovimentacao.size()));
            int quantidadeNecessaria = peca.getQuantidadeAtual() + 5 + random.nextInt(26);
            String fornecedor = FORNECEDORES_TERCEIRO.get(mesesAtras % FORNECEDORES_TERCEIRO.size());

            BigDecimal custoBase = peca.getCustoUnitario() == null ? new BigDecimal("50.00") : peca.getCustoUnitario();
            BigDecimal fator = BigDecimal.valueOf(90 + random.nextInt(21)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal valor = custoBase
                    .multiply(BigDecimal.valueOf(quantidadeNecessaria))
                    .multiply(fator)
                    .setScale(2, RoundingMode.HALF_UP);

            YearMonth mesAlvo = YearMonth.from(agora).minusMonths(mesesAtras);
            int ultimoDiaSorteavel = mesesAtras == 0
                    ? Math.max(1, agora.getDayOfMonth() - 1)
                    : mesAlvo.lengthOfMonth();
            LocalDateTime dataRecebimento = mesAlvo.atDay(1 + random.nextInt(ultimoDiaSorteavel))
                    .atTime(8 + random.nextInt(10), random.nextInt(60));
            LocalDateTime limite = agora.minusDays(1);
            if (dataRecebimento.isAfter(limite)) {
                dataRecebimento = limite;
            }

            SolicitacaoCompra solicitacao = solicitacaoCompraService.criar(
                    peca.getId(), null, quantidadeNecessaria, fornecedor, null
            );
            SolicitacaoCompra recebida = solicitacaoCompraService.marcarComoRecebida(solicitacao.getId(), valor);
            definirDataRecebimentoHistorica(recebida, dataRecebimento);
        }
    }

    // marcarComoRecebida grava dataRecebimento = now() e a entrada gerada usa dataHora updatable=false: UPDATE nativo
    private void definirDataRecebimentoHistorica(SolicitacaoCompra solicitacao, LocalDateTime dataRecebimento) {
        entityManager.flush();
        entityManager.createNativeQuery("UPDATE solicitacao_compra SET data_recebimento = :data WHERE id = :id")
                .setParameter("data", dataRecebimento)
                .setParameter("id", solicitacao.getId())
                .executeUpdate();
        solicitacao.setDataRecebimento(dataRecebimento);

        entityManager.createNativeQuery(
                        "UPDATE movimentacao_estoque SET data_hora = :data WHERE tipo = 'ENTRADA' AND observacao = :observacao")
                .setParameter("data", dataRecebimento)
                .setParameter("observacao", "Entrada referente à solicitação de compra #" + solicitacao.getId())
                .executeUpdate();
    }

    // ------------------------------------------------------------------
    // Helpers de criacao (padrao: montar entidade -> salvar -> devolver entidade original)
    // ------------------------------------------------------------------

    private Setor criarSetor(String nome) {
        Setor setor = new Setor();
        setor.setNome(nome);
        setor.setAtivo(true);
        return setorRepository.save(setor);
    }

    private Maquina criarMaquina(String codigo, String descricao, Setor setor, StatusMaquina status) {
        Maquina maquina = new Maquina();
        maquina.setCodigo(codigo);
        maquina.setDescricao(descricao);
        maquina.setSetor(setor);
        maquina.setStatus(status);
        return maquinaRepository.save(maquina);
    }

    private Peca criarPeca(
            String codigo,
            String nome,
            String categoria,
            UnidadeMedida unidadeMedida,
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

    private Tecnico criarTecnico(String nome, String salarioMensal, String cargaHorariaDiaria) {
        Tecnico tecnico = new Tecnico();
        tecnico.setNome(nome);
        tecnico.setSalarioMensal(new BigDecimal(salarioMensal));
        tecnico.setCargaHorariaDiaria(new BigDecimal(cargaHorariaDiaria));
        tecnico.setAtivo(true);
        return tecnicoRepository.save(tecnico);
    }

    private Manutencao criarManutencao(
            Maquina maquina, String problemaDescricao, TipoManutencao tipo
    ) {
        Manutencao manutencao = new Manutencao();
        manutencao.setMaquina(maquina);
        manutencao.setProblemaDescricao(problemaDescricao);
        manutencao.setTipo(tipo);
        return manutencaoRepository.saveAndFlush(manutencao);
    }

    // dataAbertura e updatable=false (fixada no @PrePersist), entao precisa de UPDATE nativo para ser retroagida
    private void definirDataAberturaHistorica(Manutencao manutencao, LocalDateTime dataAbertura) {
        entityManager.createNativeQuery("UPDATE manutencao SET data_abertura = :dataAbertura WHERE id = :id")
                .setParameter("dataAbertura", dataAbertura)
                .setParameter("id", manutencao.getId())
                .executeUpdate();
        manutencao.setDataAbertura(dataAbertura);
    }

    private void vincularTecnico(Manutencao manutencao, Tecnico tecnico, Random random) {
        ManutencaoTecnico manutencaoTecnico = new ManutencaoTecnico();
        manutencaoTecnico.setManutencao(manutencao);
        manutencaoTecnico.setTecnico(tecnico);
        manutencaoTecnico.setHorasTrabalhadas(BigDecimal.valueOf(1 + random.nextInt(8)));
        manutencaoTecnicoRepository.save(manutencaoTecnico);
    }

    private void vincularServicoTerceiro(Manutencao manutencao, Random random) {
        ServicoTerceiro servicoTerceiro = new ServicoTerceiro();
        servicoTerceiro.setManutencao(manutencao);
        servicoTerceiro.setValor(BigDecimal.valueOf(150 + random.nextInt(651)));
        servicoTerceiro.setDescricao(DESCRICOES_TERCEIRO.get(random.nextInt(DESCRICOES_TERCEIRO.size())));
        servicoTerceiro.setFornecedor(FORNECEDORES_TERCEIRO.get(random.nextInt(FORNECEDORES_TERCEIRO.size())));
        servicoTerceiroRepository.save(servicoTerceiro);
    }

    private void concluirManutencao(
            Manutencao manutencao,
            String descricaoServico,
            String condicoesSeguranca,
            boolean maquinaLiberadaParaUso,
            LocalDateTime dataInicio,
            LocalDateTime dataConclusao
    ) {
        manutencao.setDescricaoServico(descricaoServico);
        manutencao.setCondicoesSeguranca(condicoesSeguranca);
        manutencao.setMaquinaLiberadaParaUso(maquinaLiberadaParaUso);
        manutencao.setDataInicio(dataInicio);
        manutencao.setStatus(StatusManutencao.CONCLUIDA);
        manutencao.setDataConclusao(dataConclusao);
        manutencaoRepository.save(manutencao);
    }
}
