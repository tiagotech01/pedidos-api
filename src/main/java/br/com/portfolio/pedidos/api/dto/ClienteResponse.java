package br.com.portfolio.pedidos.api.dto;

import br.com.portfolio.pedidos.api.domain.Cliente;
import br.com.portfolio.pedidos.api.domain.Endereco;

public record ClienteResponse(Long id, String nome, String email, EnderecoResponse endereco) {

    public record EnderecoResponse(
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf) {
    }

    public static ClienteResponse from(Cliente cliente) {
        Endereco e = cliente.getEndereco();
        EnderecoResponse endereco = new EnderecoResponse(
                e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf());
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(), endereco);
    }
}