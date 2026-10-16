package br.com.portfolio.pedidos.api.service;

import br.com.portfolio.pedidos.api.domain.Cliente;
import br.com.portfolio.pedidos.api.domain.Pedido;
import br.com.portfolio.pedidos.api.domain.Produto;
import br.com.portfolio.pedidos.api.dto.PedidoRequest;
import br.com.portfolio.pedidos.api.dto.PedidoRequest.ItemPedidoRequest;
import br.com.portfolio.pedidos.api.dto.PedidoResponse;
import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.repository.ClienteRepository;
import br.com.portfolio.pedidos.api.repository.PedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoService produtoService;

    public PedidoService(PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository, ProdutoService produtoService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoService = produtoService;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));

        Pedido pedido = new Pedido(cliente);
        for (ItemPedidoRequest item : request.itens()) {
            Produto produto = produtoService.buscarEntidade(item.produtoId());
            pedido.adicionarItem(produto, item.quantidade());
        }

        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = buscarEntidade(id);
        pedido.cancelar();
        return PedidoResponse.from(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return PedidoResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }
}