package br.com.portfolio.pedidos.api.integration.viacep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String erro) {

    public boolean naoEncontrado() {
        return "true".equalsIgnoreCase(erro);
    }
}
