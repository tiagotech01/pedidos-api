package br.com.portfolio.pedidos.api.service;

import br.com.portfolio.pedidos.api.domain.Produto;
import br.com.portfolio.pedidos.api.dto.ProdutoRequest;
import br.com.portfolio.pedidos.api.dto.ProdutoResponse;
import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.repository.ProdutoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException(
                    "Já existe um produto com o nome '%s'".formatted(request.nome()));
        }
        Produto produto = new Produto(
                request.nome(), request.descricao(), request.preco(), request.estoque());
        return ProdutoResponse.from(repository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarAtivos() {
        return repository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return ProdutoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        produto.atualizar(request.nome(), request.descricao(), request.preco());
        return ProdutoResponse.from(produto);
    }

    @Transactional
    public void desativar(Long id) {
        buscarEntidade(id).desativar();
    }

    public Produto buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }
}