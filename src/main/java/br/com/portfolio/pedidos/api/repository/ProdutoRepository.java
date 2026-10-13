package br.com.portfolio.pedidos.api.repository;

import br.com.portfolio.pedidos.api.domain.Produto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}