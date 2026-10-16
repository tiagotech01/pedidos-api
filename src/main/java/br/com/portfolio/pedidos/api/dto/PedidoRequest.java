package br.com.portfolio.pedidos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record PedidoRequest(
        @NotNull Long clienteId,
        @NotEmpty @Valid List<ItemPedidoRequest> itens) {

    public record ItemPedidoRequest(
            @NotNull Long produtoId,
            @NotNull @Positive Integer quantidade) {
    }
}