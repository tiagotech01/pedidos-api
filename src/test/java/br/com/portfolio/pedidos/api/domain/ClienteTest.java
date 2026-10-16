package br.com.portfolio.pedidos.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ClienteTest {

    private Endereco endereco() {
        return new Endereco("01001-000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP");
    }

    @Test
    @DisplayName("deve criar cliente removendo espaços e normalizando o CEP")
    void deveCriarCliente() {
        Cliente cliente = new Cliente("  Maria Silva ", " maria@email.com ", endereco());

        assertThat(cliente.getNome()).isEqualTo("Maria Silva");
        assertThat(cliente.getEmail()).isEqualTo("maria@email.com");
        assertThat(cliente.getEndereco().getCep()).isEqualTo("01001000");
        assertThat(cliente.getCriadoEm()).isNotNull();
    }

    @ParameterizedTest(name = "nome inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("não deve criar cliente sem nome")
    void naoDeveCriarSemNome(String nome) {
        assertThatThrownBy(() -> new Cliente(nome, "maria@email.com", endereco()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O nome do cliente é obrigatório");
    }

    @ParameterizedTest(name = "e-mail inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("não deve criar cliente sem e-mail")
    void naoDeveCriarSemEmail(String email) {
        assertThatThrownBy(() -> new Cliente("Maria", email, endereco()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O e-mail do cliente é obrigatório");
    }

    @Test
    @DisplayName("não deve criar cliente sem endereço")
    void naoDeveCriarSemEndereco() {
        assertThatThrownBy(() -> new Cliente("Maria", "maria@email.com", null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O endereço do cliente é obrigatório");
    }

    @ParameterizedTest(name = "CEP inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"123", "123456789"})
    @DisplayName("não deve criar endereço com CEP inválido")
    void naoDeveCriarEnderecoComCepInvalido(String cep) {
        assertThatThrownBy(() -> new Endereco(cep, "Rua", "1", null, "Bairro", "Cidade", "SP"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O CEP deve conter 8 dígitos");
    }

    @ParameterizedTest(name = "número inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("não deve criar endereço sem número")
    void naoDeveCriarEnderecoSemNumero(String numero) {
        assertThatThrownBy(() -> new Endereco("01001000", "Rua", numero, null, "Bairro", "Cidade", "SP"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O número do endereço é obrigatório");
    }
}
