package br.com.portfolio.pedidos.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoTest {

    private final Cliente cliente = new Cliente("Maria Silva", "maria@email.com",
            new Endereco("01001000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP"));

    private Produto produto(String nome, String preco, int estoque) {
        return new Produto(nome, null, new BigDecimal(preco), estoque);
    }

    @Test
    @DisplayName("deve criar pedido confirmado e com total zero")
    void deveCriarPedidoConfirmado() {
        Pedido pedido = new Pedido(cliente);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(pedido.getTotal()).isEqualByComparingTo("0");
        assertThat(pedido.getItens()).isEmpty();
    }

    @Test
    @DisplayName("deve calcular o total e baixar o estoque de cada produto")
    void deveCalcularTotalEBaixarEstoque() {
        Produto teclado = produto("Teclado", "199.90", 10);
        Produto mouse = produto("Mouse", "50.00", 5);
        Pedido pedido = new Pedido(cliente);

        pedido.adicionarItem(teclado, 2);
        pedido.adicionarItem(mouse, 1);

        assertThat(pedido.getTotal()).isEqualByComparingTo("449.80");
        assertThat(pedido.getItens()).hasSize(2);
        assertThat(teclado.getEstoque()).isEqualTo(8);
        assertThat(mouse.getEstoque()).isEqualTo(4);
    }

    @Test
    @DisplayName("deve somar a quantidade quando o mesmo produto é adicionado de novo")
    void deveSomarQuantidadeDoMesmoProduto() {
        Produto teclado = produto("Teclado", "199.90", 10);
        Pedido pedido = new Pedido(cliente);

        pedido.adicionarItem(teclado, 1);
        pedido.adicionarItem(teclado, 2);

        assertThat(pedido.getItens()).hasSize(1);
        assertThat(pedido.getItens().get(0).getQuantidade()).isEqualTo(3);
        assertThat(pedido.getTotal()).isEqualByComparingTo("599.70");
        assertThat(teclado.getEstoque()).isEqualTo(7);
    }

    @Test
    @DisplayName("deve manter o preço da compra mesmo se o produto mudar de preço")
    void deveManterPrecoDaCompra() {
        Produto teclado = produto("Teclado", "199.90", 10);
        Pedido pedido = new Pedido(cliente);
        pedido.adicionarItem(teclado, 1);

        teclado.atualizar("Teclado", null, new BigDecimal("299.90"));

        assertThat(pedido.getTotal()).isEqualByComparingTo("199.90");
        assertThat(pedido.getItens().get(0).getPrecoUnitario()).isEqualByComparingTo("199.90");
    }

    @Test
    @DisplayName("não deve vender produto inativo")
    void naoDeveVenderProdutoInativo() {
        Produto teclado = produto("Teclado", "199.90", 10);
        teclado.desativar();
        Pedido pedido = new Pedido(cliente);

        assertThatThrownBy(() -> pedido.adicionarItem(teclado, 1))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não está disponível");
        assertThat(pedido.getItens()).isEmpty();
        assertThat(teclado.getEstoque()).isEqualTo(10);
    }

    @Test
    @DisplayName("não deve adicionar item sem estoque suficiente")
    void naoDeveAdicionarSemEstoque() {
        Produto teclado = produto("Teclado", "199.90", 1);
        Pedido pedido = new Pedido(cliente);

        assertThatThrownBy(() -> pedido.adicionarItem(teclado, 5))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(pedido.getItens()).isEmpty();
        assertThat(pedido.getTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("deve devolver o estoque ao cancelar")
    void deveDevolverEstoqueAoCancelar() {
        Produto teclado = produto("Teclado", "199.90", 10);
        Pedido pedido = new Pedido(cliente);
        pedido.adicionarItem(teclado, 3);

        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(teclado.getEstoque()).isEqualTo(10);
    }

    @Test
    @DisplayName("não deve cancelar um pedido já cancelado")
    void naoDeveCancelarDuasVezes() {
        Pedido pedido = new Pedido(cliente);
        pedido.cancelar();

        assertThatThrownBy(pedido::cancelar)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O pedido já está cancelado");
    }

    @Test
    @DisplayName("não deve alterar um pedido cancelado")
    void naoDeveAlterarPedidoCancelado() {
        Produto teclado = produto("Teclado", "199.90", 10);
        Pedido pedido = new Pedido(cliente);
        pedido.cancelar();

        assertThatThrownBy(() -> pedido.adicionarItem(teclado, 1))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível alterar um pedido cancelado");
        assertThat(teclado.getEstoque()).isEqualTo(10);
    }
}
