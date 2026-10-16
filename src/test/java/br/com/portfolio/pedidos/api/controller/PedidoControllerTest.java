package br.com.portfolio.pedidos.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.portfolio.pedidos.api.domain.StatusPedido;
import br.com.portfolio.pedidos.api.dto.PedidoRequest;
import br.com.portfolio.pedidos.api.dto.PedidoResponse;
import br.com.portfolio.pedidos.api.dto.PedidoResponse.ItemPedidoResponse;
import br.com.portfolio.pedidos.api.service.PedidoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService service;

    private PedidoResponse pedido(StatusPedido status) {
        return new PedidoResponse(1L, 1L, "Maria Silva", status, new BigDecimal("599.70"),
                LocalDateTime.of(2026, 10, 16, 12, 0),
                List.of(new ItemPedidoResponse(
                        1L, "Teclado", 3, new BigDecimal("199.90"), new BigDecimal("599.70"))));
    }

    @Test
    @DisplayName("POST deve criar o pedido e devolver 201")
    void deveCriarPedido() throws Exception {
        when(service.criar(any(PedidoRequest.class))).thenReturn(pedido(StatusPedido.CONFIRMADO));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 1, "itens": [{"produtoId": 1, "quantidade": 3}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/pedidos/1"))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3));
    }

    @Test
    @DisplayName("POST deve validar cada item da lista")
    void deveValidarItens() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 1, "itens": [{"produtoId": 1, "quantidade": 0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['itens[0].quantidade']").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST deve recusar pedido sem itens")
    void deveRecusarPedidoSemItens() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId": 1, "itens": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.itens").exists());
    }

    @Test
    @DisplayName("GET deve buscar o pedido pelo id")
    void deveBuscarPedido() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(pedido(StatusPedido.CONFIRMADO));

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteNome").value("Maria Silva"));
    }

    @Test
    @DisplayName("GET deve listar os pedidos do cliente")
    void deveListarPedidosDoCliente() throws Exception {
        when(service.listarPorCliente(1L)).thenReturn(List.of(pedido(StatusPedido.CONFIRMADO)));

        mockMvc.perform(get("/api/pedidos").param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("POST /cancelamento deve cancelar o pedido")
    void deveCancelarPedido() throws Exception {
        when(service.cancelar(1L)).thenReturn(pedido(StatusPedido.CANCELADO));

        mockMvc.perform(post("/api/pedidos/1/cancelamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}