package br.com.portfolio.pedidos.api.service;

import br.com.portfolio.pedidos.api.domain.Cliente;
import br.com.portfolio.pedidos.api.domain.Endereco;
import br.com.portfolio.pedidos.api.dto.ClienteRequest;
import br.com.portfolio.pedidos.api.dto.ClienteResponse;
import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.integration.viacep.ViaCepClient;
import br.com.portfolio.pedidos.api.integration.viacep.ViaCepResponse;
import br.com.portfolio.pedidos.api.repository.ClienteRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final ViaCepClient viaCepClient;

    public ClienteService(ClienteRepository repository, ViaCepClient viaCepClient) {
        this.repository = repository;
        this.viaCepClient = viaCepClient;
    }

    public ClienteResponse criar(ClienteRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException(
                    "Já existe um cliente com o e-mail '%s'".formatted(email));
        }

        ViaCepResponse viaCep = viaCepClient.buscar(request.cep());
        Endereco endereco = new Endereco(
                request.cep(), viaCep.logradouro(), request.numero(), request.complemento(),
                viaCep.bairro(), viaCep.localidade(), viaCep.uf());

        Cliente cliente = new Cliente(request.nome(), email, endereco);
        return ClienteResponse.from(repository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll(Sort.by("nome")).stream()
                .map(ClienteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(ClienteResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}