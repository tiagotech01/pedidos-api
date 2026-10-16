package br.com.portfolio.pedidos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.portfolio.pedidos.api.domain.Cliente;
import br.com.portfolio.pedidos.api.dto.ClienteRequest;
import br.com.portfolio.pedidos.api.dto.ClienteResponse;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.integration.viacep.ViaCepClient;
import br.com.portfolio.pedidos.api.integration.viacep.ViaCepResponse;
import br.com.portfolio.pedidos.api.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private ClienteService service;

    private final ClienteRequest request =
            new ClienteRequest("Maria Silva", "Maria@Email.com", "01001-000", "100", "Apto 12");

    @Test
    @DisplayName("deve criar cliente com endereço preenchido pelo ViaCEP")
    void deveCriarClienteComEnderecoDoViaCep() {
        when(repository.existsByEmailIgnoreCase("maria@email.com")).thenReturn(false);
        when(viaCepClient.buscar("01001-000")).thenReturn(new ViaCepResponse(
                "01001-000", "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP", null));
        when(repository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = service.criar(request);

        assertThat(response.email()).isEqualTo("maria@email.com");
        assertThat(response.endereco().cep()).isEqualTo("01001000");
        assertThat(response.endereco().logradouro()).isEqualTo("Praça da Sé");
        assertThat(response.endereco().cidade()).isEqualTo("São Paulo");
        assertThat(response.endereco().uf()).isEqualTo("SP");
        assertThat(response.endereco().numero()).isEqualTo("100");
        assertThat(response.endereco().complemento()).isEqualTo("Apto 12");
    }

    @Test
    @DisplayName("não deve consultar o ViaCEP quando o e-mail já existe")
    void naoDeveConsultarViaCepComEmailDuplicado() {
        when(repository.existsByEmailIgnoreCase("maria@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Já existe um cliente");
        verifyNoInteractions(viaCepClient);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("não deve salvar o cliente quando o CEP não existe")
    void naoDeveSalvarQuandoCepNaoExiste() {
        when(repository.existsByEmailIgnoreCase("maria@email.com")).thenReturn(false);
        when(viaCepClient.buscar("01001-000"))
                .thenThrow(new RegraNegocioException("CEP 01001000 não encontrado"));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("CEP 01001000 não encontrado");
        verify(repository, never()).save(any());
    }
}
