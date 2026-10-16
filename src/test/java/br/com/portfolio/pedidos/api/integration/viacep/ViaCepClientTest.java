package br.com.portfolio.pedidos.api.integration.viacep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.exception.ServicoExternoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ViaCepClientTest {

    private static final String BASE_URL = "https://viacep.teste/ws";

    private MockRestServiceServer servidor;
    private ViaCepClient client;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder, BASE_URL);
    }

    @Test
    @DisplayName("deve buscar o endereço, aceitando CEP com hífen e ignorando campos extras")
    void deveBuscarEndereco() {
        servidor.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withSuccess("""
                        {"cep": "01001-000", "logradouro": "Praça da Sé", "complemento": "lado ímpar",
                         "bairro": "Sé", "localidade": "São Paulo", "uf": "SP", "ibge": "3550308"}
                        """, MediaType.APPLICATION_JSON));

        ViaCepResponse resposta = client.buscar("01001-000");

        assertThat(resposta.logradouro()).isEqualTo("Praça da Sé");
        assertThat(resposta.localidade()).isEqualTo("São Paulo");
        assertThat(resposta.uf()).isEqualTo("SP");
        servidor.verify();
    }

    @Test
    @DisplayName("deve lançar regra de negócio quando o CEP não existe")
    void deveFalharQuandoCepNaoExiste() {
        servidor.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.buscar("99999999"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("CEP 99999999 não encontrado");
    }

    @Test
    @DisplayName("não deve chamar o ViaCEP quando o CEP tem formato inválido")
    void naoDeveChamarComCepInvalido() {
        assertThatThrownBy(() -> client.buscar("123"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O CEP deve conter 8 dígitos");
        servidor.verify();
    }

    @Test
    @DisplayName("deve lançar exceção de serviço externo quando o ViaCEP falha")
    void deveTratarFalhaDoViaCep() {
        servidor.expect(requestTo(BASE_URL + "/01001000/json/")).andRespond(withServerError());

        assertThatThrownBy(() -> client.buscar("01001000"))
                .isInstanceOf(ServicoExternoException.class)
                .hasMessageContaining("ViaCEP");
    }
}