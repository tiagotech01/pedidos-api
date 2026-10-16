package br.com.portfolio.pedidos.api.repository;

import br.com.portfolio.pedidos.api.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
