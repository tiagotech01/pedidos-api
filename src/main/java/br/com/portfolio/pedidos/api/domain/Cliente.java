package br.com.portfolio.pedidos.api.domain;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Embedded
    private Endereco endereco;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Cliente() {
        // construtor exigido pelo JPA
    }

    public Cliente(String nome, String email, Endereco endereco) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do cliente é obrigatório");
        }
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("O e-mail do cliente é obrigatório");
        }
        if (endereco == null) {
            throw new RegraNegocioException("O endereço do cliente é obrigatório");
        }
        this.nome = nome.trim();
        this.email = email.trim();
        this.endereco = endereco;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente outro)) {
            return false;
        }
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
