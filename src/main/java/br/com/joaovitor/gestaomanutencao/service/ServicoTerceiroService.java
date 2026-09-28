package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.ServicoTerceiroValorInvalidoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ServicoTerceiro;
import br.com.joaovitor.gestaomanutencao.model.StatusManutencao;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ServicoTerceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ServicoTerceiroService {

    private final ServicoTerceiroRepository servicoTerceiroRepository;
    private final ManutencaoRepository manutencaoRepository;

    public ServicoTerceiroService(
            ServicoTerceiroRepository servicoTerceiroRepository,
            ManutencaoRepository manutencaoRepository
    ) {
        this.servicoTerceiroRepository = servicoTerceiroRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    @Transactional
    public ServicoTerceiro criar(
            Long manutencaoId,
            BigDecimal valorApurado,
            BigDecimal horasTrabalhadas,
            BigDecimal valorHora,
            String descricao,
            String fornecedor
    ) {
        // horas e valor/hora sao persistidos com 2 casas; normalizar antes evita valorApurado inconsistente com o gravado
        BigDecimal horas = escalaDois(horasTrabalhadas);
        BigDecimal valorPorHora = escalaDois(valorHora);
        BigDecimal valorCalculado = calcularValorApurado(valorApurado, horas, valorPorHora);

        Manutencao manutencao = manutencaoRepository.buscarPorIdComTrava(manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Manutenção com ID " + manutencaoId + " não encontrada."
                ));

        if (manutencao.estaFinalizada()) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível adicionar serviço de terceiro a manutenção com status: " + manutencao.getStatus()
            );
        }

        ServicoTerceiro servicoTerceiro = new ServicoTerceiro();
        servicoTerceiro.setManutencao(manutencao);
        servicoTerceiro.setValorApurado(valorCalculado);
        servicoTerceiro.setHorasTrabalhadas(horas);
        servicoTerceiro.setValorHora(valorPorHora);
        servicoTerceiro.setDescricao(descricao);
        servicoTerceiro.setFornecedor(fornecedor);

        servicoTerceiroRepository.save(servicoTerceiro);
        return servicoTerceiro;
    }

    @Transactional
    public ServicoTerceiro atualizar(Long id, BigDecimal valorFinal, String observacao) {
        ServicoTerceiro servicoTerceiro = buscarOuFalhar(id);

        if (servicoTerceiro.getManutencao().getStatus() == StatusManutencao.CANCELADA) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível alterar serviço de terceiro de manutenção CANCELADA."
            );
        }

        servicoTerceiro.setValorFinal(valorFinal);
        servicoTerceiro.setObservacao(observacao);

        servicoTerceiroRepository.save(servicoTerceiro);
        return servicoTerceiro;
    }

    @Transactional
    public void excluir(Long id) {
        ServicoTerceiro servicoTerceiro = buscarOuFalhar(id);

        if (servicoTerceiro.getManutencao().estaFinalizada()) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível remover serviço de terceiro de manutenção com status: "
                            + servicoTerceiro.getManutencao().getStatus()
            );
        }

        servicoTerceiroRepository.delete(servicoTerceiro);
    }

    private ServicoTerceiro buscarOuFalhar(Long id) {
        return servicoTerceiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Serviço de terceiro com ID " + id + " não encontrado."
                ));
    }

    private BigDecimal escalaDois(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularValorApurado(BigDecimal valorApurado, BigDecimal horasTrabalhadas, BigDecimal valorHora) {
        boolean temHoras = horasTrabalhadas != null;
        boolean temValorHora = valorHora != null;

        if (temHoras != temValorHora) {
            throw new ServicoTerceiroValorInvalidoException(
                    "Informe horasTrabalhadas e valorHora juntos, ou nenhum dos dois."
            );
        }
        if (temHoras && valorApurado != null) {
            throw new ServicoTerceiroValorInvalidoException(
                    "Informe o valorApurado diretamente ou horasTrabalhadas com valorHora, não os dois modos juntos."
            );
        }
        if (temHoras) {
            return horasTrabalhadas.multiply(valorHora).setScale(2, RoundingMode.HALF_UP);
        }
        if (valorApurado == null) {
            throw new ServicoTerceiroValorInvalidoException(
                    "Informe o valorApurado ou horasTrabalhadas com valorHora."
            );
        }
        return valorApurado;
    }
}
