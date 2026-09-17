package br.com.joaovitor.gestaomanutencao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "maquina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Maquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String descricao;

    private String setor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMaquina status = StatusMaquina.ATIVA;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadaEm = agora;
        this.atualizadaEm = agora;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadaEm = LocalDateTime.now();
    }
}
