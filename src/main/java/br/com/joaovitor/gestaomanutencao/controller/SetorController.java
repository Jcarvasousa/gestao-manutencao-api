package br.com.joaovitor.gestaomanutencao.controller;

import br.com.joaovitor.gestaomanutencao.dto.SetorRequestDTO;
import br.com.joaovitor.gestaomanutencao.dto.SetorResponseDTO;
import br.com.joaovitor.gestaomanutencao.model.Setor;
import br.com.joaovitor.gestaomanutencao.repository.SetorRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
public class SetorController {

    private final SetorRepository setorRepository;

    public SetorController(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    @PostMapping
    public ResponseEntity<SetorResponseDTO> criar(@Valid @RequestBody SetorRequestDTO requestDTO) {
        Setor setor = new Setor();
        setor.setNome(requestDTO.nome());

        setorRepository.save(setor);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SetorResponseDTO.fromEntity(setor));
    }

    @GetMapping
    public ResponseEntity<List<SetorResponseDTO>> listarTodos() {
        List<SetorResponseDTO> lista = setorRepository.findAll()
                .stream()
                .map(SetorResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(lista);
    }
}
