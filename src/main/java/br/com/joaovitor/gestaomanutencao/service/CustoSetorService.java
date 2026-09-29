package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetorDTO;
import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoSetoresResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.RelatorioParametrosInvalidosException;
import br.com.joaovitor.gestaomanutencao.model.Maquina;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.repository.MaquinaRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import br.com.joaovitor.gestaomanutencao.repository.SetorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class CustoSetorService {

    private static final String NOME_SEM_SETOR = "Sem setor";

    private final SetorRepository setorRepository;
    private final MaquinaRepository maquinaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final CustoManutencaoService custoManutencaoService;

    public CustoSetorService(
            SetorRepository setorRepository,
            MaquinaRepository maquinaRepository,
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            CustoManutencaoService custoManutencaoService
    ) {
        this.setorRepository = setorRepository;
        this.maquinaRepository = maquinaRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.custoManutencaoService = custoManutencaoService;
    }

    @Transactional(readOnly = true)
    public RelatorioCustoSetoresResponseDTO calcularRelatorioCustoSetores(
            List<Long> setorIds,
            Integer mes,
            Integer ano
    ) {
        validarPeriodo(mes, ano);

        boolean filtrandoPorSetores = setorIds != null && !setorIds.isEmpty();

        List<Setor> setoresAlvo = new ArrayList<>();
        Map<Long, List<Maquina>> maquinasPorSetorId;
        List<Maquina> maquinasSemSetor;

        if (filtrandoPorSetores) {
            for (Long setorId : new LinkedHashSet<>(setorIds)) {
                Setor setor = setorRepository.findById(setorId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Setor com ID " + setorId + " não encontrado."
                        ));
                setoresAlvo.add(setor);
            }
            List<Long> idsUnicos = setoresAlvo.stream().map(Setor::getId).toList();
            List<Maquina> maquinas = maquinaRepository.findBySetorIdIn(idsUnicos);
            maquinasPorSetorId = agruparPorSetorId(maquinas);
            maquinasSemSetor = List.of();
        } else {
            setoresAlvo.addAll(setorRepository.findAll());
            List<Maquina> maquinas = maquinaRepository.findAll();
            maquinasPorSetorId = agruparPorSetorId(maquinas);
            maquinasSemSetor = maquinas.stream()
                    .filter(maquina -> maquina.getSetor() == null)
                    .toList();
        }

        List<RelatorioCustoSetorDTO> setoresCalculados = new ArrayList<>();
        for (Setor setor : setoresAlvo) {
            List<Maquina> maquinasDoSetor = maquinasPorSetorId.getOrDefault(setor.getId(), List.of());
            setoresCalculados.add(calcularCustoSetor(
                    setor.getId(), setor.getNome(), setor.getAtivo(), maquinasDoSetor, mes, ano
            ));
        }

        setoresCalculados.sort(
                Comparator.comparing(RelatorioCustoSetorDTO::custoTotal, Comparator.reverseOrder())
                        .thenComparing(RelatorioCustoSetorDTO::setorNome)
        );

        if (!filtrandoPorSetores && !maquinasSemSetor.isEmpty()) {
            setoresCalculados.add(calcularCustoSetor(null, NOME_SEM_SETOR, true, maquinasSemSetor, mes, ano));
        }

        BigDecimal totalGeral = setoresCalculados.stream()
                .map(RelatorioCustoSetorDTO::custoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<RelatorioCustoSetorDTO> setoresComPercentual = new ArrayList<>();
        for (RelatorioCustoSetorDTO setor : setoresCalculados) {
            BigDecimal percentual = totalGeral.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : setor.custoTotal()
                    .divide(totalGeral, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            setoresComPercentual.add(new RelatorioCustoSetorDTO(
                    setor.setorId(),
                    setor.setorNome(),
                    setor.ativo(),
                    setor.quantidadeMaquinas(),
                    setor.custoPecas(),
                    setor.custoMaoDeObra(),
                    setor.custoTotal(),
                    percentual
            ));
        }

        return new RelatorioCustoSetoresResponseDTO(mes, ano, setoresComPercentual, totalGeral);
    }

    private Map<Long, List<Maquina>> agruparPorSetorId(List<Maquina> maquinas) {
        Map<Long, List<Maquina>> mapa = new LinkedHashMap<>();
        for (Maquina maquina : maquinas) {
            if (maquina.getSetor() == null) continue;
            mapa.computeIfAbsent(maquina.getSetor().getId(), id -> new ArrayList<>()).add(maquina);
        }
        return mapa;
    }

    private RelatorioCustoSetorDTO calcularCustoSetor(
            Long setorId,
            String setorNome,
            Boolean ativo,
            List<Maquina> maquinas,
            Integer mes,
            Integer ano
    ) {
        BigDecimal custoPecas = BigDecimal.ZERO;
        BigDecimal custoMaoDeObra = BigDecimal.ZERO;

        for (Maquina maquina : maquinas) {
            Long maquinaId = maquina.getId();
            BigDecimal pecasMaquina;
            BigDecimal maoDeObraMaquina;

            if (mes != null && ano != null) {
                pecasMaquina = movimentacaoEstoqueRepository
                        .calcularCustoPecasPorMaquinaMesEAno(maquinaId, mes, ano);
                maoDeObraMaquina = custoManutencaoService
                        .calcularCustoMaoDeObraPorMaquinaMesEAno(maquinaId, mes, ano);
            } else if (ano != null) {
                pecasMaquina = movimentacaoEstoqueRepository
                        .calcularCustoPecasPorMaquinaEAno(maquinaId, ano);
                maoDeObraMaquina = custoManutencaoService
                        .calcularCustoMaoDeObraPorMaquinaEAno(maquinaId, ano);
            } else {
                pecasMaquina = movimentacaoEstoqueRepository
                        .calcularCustoPecasPorMaquinaTotal(maquinaId);
                maoDeObraMaquina = custoManutencaoService
                        .calcularCustoMaoDeObraPorMaquinaTotal(maquinaId);
            }

            if (pecasMaquina != null) custoPecas = custoPecas.add(pecasMaquina);
            if (maoDeObraMaquina != null) custoMaoDeObra = custoMaoDeObra.add(maoDeObraMaquina);
        }

        custoPecas = custoPecas.setScale(2, RoundingMode.HALF_UP);
        custoMaoDeObra = custoMaoDeObra.setScale(2, RoundingMode.HALF_UP);
        BigDecimal custoTotal = custoPecas.add(custoMaoDeObra).setScale(2, RoundingMode.HALF_UP);

        return new RelatorioCustoSetorDTO(
                setorId,
                setorNome,
                ativo,
                maquinas.size(),
                custoPecas,
                custoMaoDeObra,
                custoTotal,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private void validarPeriodo(Integer mes, Integer ano) {
        if (mes != null && ano == null) {
            throw new RelatorioParametrosInvalidosException("ano é obrigatório quando mes é informado.");
        }
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new RelatorioParametrosInvalidosException("mes deve estar entre 1 e 12.");
        }
    }
}
