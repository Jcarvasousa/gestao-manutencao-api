package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoTecnicoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustoManutencaoService {

    private final ServicoTerceiroRepository servicoTerceiroRepository;
    private final ManutencaoTecnicoRepository manutencaoTecnicoRepository;

    public CustoManutencaoService(
            ServicoTerceiroRepository servicoTerceiroRepository,
            ManutencaoTecnicoRepository manutencaoTecnicoRepository
    ) {
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.manutencaoTecnicoRepository = manutencaoTecnicoRepository;
    }

    public BigDecimal calcularCustoMaoDeObraPorMesEAno(Integer mes, Integer ano) {
        BigDecimal custoServicoTerceiro = servicoTerceiroRepository.somarValorServicoTerceiroPorMesEAno(mes, ano);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                manutencaoTecnicoRepository.buscarManutencaoTecnicosConcluidosPorMesEAno(mes, ano)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

    public BigDecimal calcularCustoMaoDeObraPorMaquinaMesEAno(Long maquinaId, Integer mes, Integer ano) {
        BigDecimal custoServicoTerceiro = servicoTerceiroRepository
                .somarValorServicoTerceiroPorMaquinaMesEAno(maquinaId, mes, ano);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                manutencaoTecnicoRepository.buscarManutencaoTecnicosConcluidosPorMaquinaMesEAno(maquinaId, mes, ano)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

    public BigDecimal calcularCustoMaoDeObraPorMaquinaEAno(Long maquinaId, Integer ano) {
        BigDecimal custoServicoTerceiro = servicoTerceiroRepository
                .somarValorServicoTerceiroPorMaquinaEAno(maquinaId, ano);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                manutencaoTecnicoRepository.buscarManutencaoTecnicosConcluidosPorMaquinaEAno(maquinaId, ano)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

    public BigDecimal calcularCustoMaoDeObraPorMaquinaTotal(Long maquinaId) {
        BigDecimal custoServicoTerceiro = servicoTerceiroRepository
                .somarValorServicoTerceiroPorMaquinaTotal(maquinaId);
        BigDecimal custoMaoDeObraInterna = somarCustoMaoDeObraInterna(
                manutencaoTecnicoRepository.buscarManutencaoTecnicosConcluidosPorMaquinaTotal(maquinaId)
        );
        return custoServicoTerceiro.add(custoMaoDeObraInterna);
    }

    private BigDecimal somarCustoMaoDeObraInterna(List<ManutencaoTecnico> vinculos) {
        return vinculos.stream()
                .map(vinculo -> vinculo.getHorasTrabalhadas().multiply(vinculo.getTecnico().getCustoPorHora()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
