package br.com.joaovitor.gestaomanutencao.service;

import br.com.joaovitor.gestaomanutencao.exception.ManutencaoNaoEstaAbertaException;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.model.Manutencao;
import br.com.joaovitor.gestaomanutencao.model.ManutencaoTecnico;
import br.com.joaovitor.gestaomanutencao.model.Tecnico;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoRepository;
import br.com.joaovitor.gestaomanutencao.repository.ManutencaoTecnicoRepository;
import br.com.joaovitor.gestaomanutencao.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ManutencaoTecnicoService {

    private final ManutencaoTecnicoRepository manutencaoTecnicoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final TecnicoRepository tecnicoRepository;

    public ManutencaoTecnicoService(
            ManutencaoTecnicoRepository manutencaoTecnicoRepository,
            ManutencaoRepository manutencaoRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.manutencaoTecnicoRepository = manutencaoTecnicoRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ManutencaoTecnico criar(Long manutencaoId, Long tecnicoId, BigDecimal horasTrabalhadas) {
        Manutencao manutencao = buscarManutencaoAberta(manutencaoId, "adicionar técnico a");

        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico com ID " + tecnicoId + " não encontrado."));

        ManutencaoTecnico manutencaoTecnico = new ManutencaoTecnico();
        manutencaoTecnico.setManutencao(manutencao);
        manutencaoTecnico.setTecnico(tecnico);
        manutencaoTecnico.setHorasTrabalhadas(horasTrabalhadas);

        manutencaoTecnicoRepository.save(manutencaoTecnico);
        return manutencaoTecnico;
    }

    @Transactional
    public void excluir(Long manutencaoId, Long vinculoId) {
        buscarManutencaoAberta(manutencaoId, "remover técnico de");

        ManutencaoTecnico vinculo = manutencaoTecnicoRepository.findByIdAndManutencaoId(vinculoId, manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vínculo de técnico com ID " + vinculoId + " não encontrado na manutenção " + manutencaoId + "."
                ));

        manutencaoTecnicoRepository.delete(vinculo);
    }

    private Manutencao buscarManutencaoAberta(Long manutencaoId, String acao) {
        Manutencao manutencao = manutencaoRepository.buscarPorIdComTrava(manutencaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Manutenção com ID " + manutencaoId + " não encontrada."
                ));

        if (manutencao.estaFinalizada()) {
            throw new ManutencaoNaoEstaAbertaException(
                    "Não é possível " + acao + " manutenção com status: " + manutencao.getStatus()
            );
        }
        return manutencao;
    }
}
