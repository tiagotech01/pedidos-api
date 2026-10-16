package br.com.portfolio.pedidos.api.dto;

import br.com.portfolio.pedidos.api.domain.ItemPedido;
import br.com.portfolio.pedidos.api.domain.Pedido;
import br.com.portfolio.pedidos.api.domain.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        StatusPedido status,
        BigDecimal total,
        LocalDateTime criadoEm,
        List<ItemPedidoResponse> itens) {

    public record ItemPedidoResponse(
            Long produtoId,
            String produtoNome,
            int quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal) {

        static ItemPedidoResponse from(ItemPedido item) {
            return new ItemPedidoResponse(
                    item.getProduto().getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    item.getPrecoUnitario(),
                    item.getSubtotal());
        }
    }

    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getStatus(),
                pedido.getTotal(),
                pedido.getCriadoEm(),
                pedido.getItens().stream().map(ItemPedidoResponse::from).toList());
    }
}
