package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.RelatorioCustoMensalDTO;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.MovimentacaoEstoqueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ManutencaoRepository manutencaoRepository;

    public RelatorioController(
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            ManutencaoRepository manutencaoRepository
    ) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    @GetMapping("/custo-mensal")
    public ResponseEntity<RelatorioCustoMensalDTO> custoMensal(
            @RequestParam Integer mes,
            @RequestParam Integer ano
    ) {
        BigDecimal custoPecas = movimentacaoEstoqueRepository.calcularCustoPecasPorMesEAno(mes, ano);
        if (custoPecas == null) custoPecas = BigDecimal.ZERO;

        BigDecimal custoMaoDeObra = manutencaoRepository.calcularCustoMaoDeObraPorMesEAno(mes, ano);
        if (custoMaoDeObra == null) custoMaoDeObra = BigDecimal.ZERO;

        BigDecimal custoTotal = custoPecas.add(custoMaoDeObra);

        RelatorioCustoMensalDTO relatorio = new RelatorioCustoMensalDTO(
                mes,
                ano,
                custoPecas,
                custoMaoDeObra,
                custoTotal
        );

        return ResponseEntity.ok(relatorio);
    }
}
