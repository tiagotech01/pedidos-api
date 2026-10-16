package br.com.portfolio.pedidos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.portfolio.pedidos.api.domain.Cliente;
import br.com.portfolio.pedidos.api.domain.Endereco;
import br.com.portfolio.pedidos.api.domain.Pedido;
import br.com.portfolio.pedidos.api.domain.Produto;
import br.com.portfolio.pedidos.api.domain.StatusPedido;
import br.com.portfolio.pedidos.api.dto.PedidoRequest;
import br.com.portfolio.pedidos.api.dto.PedidoRequest.ItemPedidoRequest;
import br.com.portfolio.pedidos.api.dto.PedidoResponse;
import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.repository.ClienteRepository;
import br.com.portfolio.pedidos.api.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private PedidoService service;

    private final Cliente cliente = new Cliente("Maria Silva", "maria@email.com",
            new Endereco("01001000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP"));

    private PedidoRequest request(int quantidade) {
        return new PedidoRequest(1L, List.of(new ItemPedidoRequest(10L, quantidade)));
    }

    @Test
    @DisplayName("deve criar o pedido e baixar o estoque")
    void deveCriarPedido() {
        Produto teclado = new Produto("Teclado", null, new BigDecimal("199.90"), 10);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoService.buscarEntidade(10L)).thenReturn(teclado);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = service.criar(request(2));

        assertThat(response.status()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(response.total()).isEqualByComparingTo("399.80");
        assertThat(response.clienteNome()).isEqualTo("Maria Silva");
        assertThat(response.itens()).hasSize(1);
        assertThat(teclado.getEstoque()).isEqualTo(8);
    }

    @Test
    @DisplayName("não deve buscar produtos quando o cliente não existe")
    void deveFalharQuandoClienteNaoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request(1)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Cliente 1 não encontrado");
        verifyNoInteractions(produtoService);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("não deve salvar o pedido quando falta estoque")
    void naoDeveSalvarSemEstoque() {
        Produto teclado = new Produto("Teclado", null, new BigDecimal("199.90"), 1);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoService.buscarEntidade(10L)).thenReturn(teclado);

        assertThatThrownBy(() -> service.criar(request(5)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve cancelar o pedido e devolver o estoque")
    void deveCancelarPedido() {
        Produto teclado = new Produto("Teclado", null, new BigDecimal("199.90"), 10);
        Pedido pedido = new Pedido(cliente);
        pedido.adicionarItem(teclado, 4);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoResponse response = service.cancelar(1L);

        assertThat(response.status()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(teclado.getEstoque()).isEqualTo(10);
    }

    @Test
    @DisplayName("deve lançar exceção ao cancelar pedido inexistente")
    void deveFalharAoCancelarPedidoInexistente() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Pedido 99 não encontrado");
    }
}
