package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.SetorRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.SetorResponseDTO;
import br.com.joaovitor.gestaomanutencao.exception.RecursoNaoEncontradoException;
import br.com.joaovitor.gestaomanutencao.exception.SetorNomeDuplicadoException;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.repository.SetorRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
public class SetorController {

    private static final Sort ORDEM_POR_NOME = Sort.by("nome");

    private final SetorRepository setorRepository;

    public SetorController(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    @PostMapping
    public ResponseEntity<SetorResponseDTO> criar(@Valid @RequestBody SetorRequestDTO requestDTO) {
        String nome = requestDTO.nome().trim();
        if (setorRepository.existsByNomeIgnoreCase(nome)) {
            throw new SetorNomeDuplicadoException("Já existe um setor com o nome '" + nome + "'.");
        }

        Setor setor = new Setor();
        setor.setNome(nome);
        setor.setAtivo(requestDTO.ativo() == null ? Boolean.TRUE : requestDTO.ativo());

        setorRepository.save(setor);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SetorResponseDTO.fromEntity(setor));
    }

    @GetMapping
    public ResponseEntity<List<SetorResponseDTO>> listarTodos(
            @RequestParam(required = false) Boolean ativo
    ) {
        List<Setor> setores = ativo == null
                ? setorRepository.findAll(ORDEM_POR_NOME)
                : setorRepository.findByAtivo(ativo, ORDEM_POR_NOME);

        List<SetorResponseDTO> lista = setores
                .stream()
                .map(SetorResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SetorRequestDTO requestDTO
    ) {
        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Setor com ID " + id + " não encontrado."
                ));

        String nome = requestDTO.nome().trim();
        if (setorRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new SetorNomeDuplicadoException("Já existe um setor com o nome '" + nome + "'.");
        }

        setor.setNome(nome);
        setor.setAtivo(requestDTO.ativo() == null ? Boolean.TRUE : requestDTO.ativo());

        setorRepository.save(setor);
        return ResponseEntity.ok(SetorResponseDTO.fromEntity(setor));
    }
}
