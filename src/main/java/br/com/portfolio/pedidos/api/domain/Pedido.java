package br.com.portfolio.pedidos.api.domain;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Pedido() {
        // construtor exigido pelo JPA
    }

    public Pedido(Cliente cliente) {
        if (cliente == null) {
            throw new RegraNegocioException("O cliente do pedido é obrigatório");
        }
        this.cliente = cliente;
        this.status = StatusPedido.CONFIRMADO;
        this.criadoEm = LocalDateTime.now();
    }

    public void adicionarItem(Produto produto, int quantidade) {
        if (status == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("Não é possível alterar um pedido cancelado");
        }
        if (produto == null) {
            throw new RegraNegocioException("O produto é obrigatório");
        }
        if (!produto.isAtivo()) {
            throw new RegraNegocioException(
                    "O produto '%s' não está disponível para venda".formatted(produto.getNome()));
        }

        produto.baixarEstoque(quantidade);

        itens.stream()
                .filter(item -> item.getProduto().equals(produto))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.adicionarQuantidade(quantidade),
                        () -> itens.add(new ItemPedido(this, produto, quantidade)));

        recalcularTotal();
    }

    public void cancelar() {
        if (status == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("O pedido já está cancelado");
        }
        itens.forEach(item -> item.getProduto().reporEstoque(item.getQuantidade()));
        this.status = StatusPedido.CANCELADO;
    }

    private void recalcularTotal() {
        this.total = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pedido outro)) {
            return false;
        }
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
