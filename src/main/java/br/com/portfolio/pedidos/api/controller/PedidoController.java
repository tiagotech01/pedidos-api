package br.com.portfolio.pedidos.api.controller;

import br.com.portfolio.pedidos.api.dto.PedidoRequest;
import br.com.portfolio.pedidos.api.dto.PedidoResponse;
import br.com.portfolio.pedidos.api.service.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(
            @RequestBody @Valid PedidoRequest request, UriComponentsBuilder uriBuilder) {
        PedidoResponse criado = service.criar(request);
        URI uri = uriBuilder.path("/api/pedidos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping
    public List<PedidoResponse> listarPorCliente(@RequestParam Long clienteId) {
        return service.listarPorCliente(clienteId);
    }

    @PostMapping("/{id}/cancelamento")
    public PedidoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}