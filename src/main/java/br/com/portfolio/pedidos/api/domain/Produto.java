package br.com.portfolio.pedidos.api.domain;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int estoque;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Produto() {
        // construtor exigido pelo JPA
    }

    public Produto(String nome, String descricao, BigDecimal preco, int estoque) {
        atualizar(nome, descricao, preco);
        if (estoque < 0) {
            throw new RegraNegocioException("O estoque inicial não pode ser negativo");
        }
        this.estoque = estoque;
        this.criadoEm = LocalDateTime.now();
    }

    public void atualizar(String nome, String descricao, BigDecimal preco) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do produto é obrigatório");
        }
        if (preco == null || preco.signum() <= 0) {
            throw new RegraNegocioException("O preço deve ser maior que zero");
        }
        this.nome = nome.trim();
        this.descricao = descricao;
        this.preco = preco;
    }

    public void baixarEstoque(int quantidade) {
        validarQuantidade(quantidade);
        if (quantidade > estoque) {
            throw new RegraNegocioException(
                    "Estoque insuficiente para '%s': disponível %d, solicitado %d"
                            .formatted(nome, estoque, quantidade));
        }
        this.estoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        validarQuantidade(quantidade);
        this.estoque += quantidade;
    }

    public void desativar() {
        this.ativo = false;
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produto outro)) {
            return false;
        }
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}