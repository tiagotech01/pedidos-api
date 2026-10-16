package br.com.portfolio.pedidos.api.integration.viacep;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.exception.ServicoExternoException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ViaCepClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final RestClient restClient;

    @Autowired
    public ViaCepClient(@Value("${viacep.url:https://viacep.com.br/ws}") String baseUrl) {
        this(RestClient.builder().requestFactory(criarRequestFactory()), baseUrl);
    }

    ViaCepClient(RestClient.Builder builder, String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ViaCepResponse buscar(String cep) {
        String cepNormalizado = normalizar(cep);
        ViaCepResponse resposta;
        try {
            resposta = restClient.get()
                    .uri("/{cep}/json/", cepNormalizado)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException ex) {
            throw new ServicoExternoException("Não foi possível consultar o CEP no ViaCEP", ex);
        }
        if (resposta == null || resposta.naoEncontrado()) {
            throw new RegraNegocioException("CEP %s não encontrado".formatted(cepNormalizado));
        }
        return resposta;
    }

    private static String normalizar(String cep) {
        String digitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new RegraNegocioException("O CEP deve conter 8 dígitos");
        }
        return digitos;
    }

    private static SimpleClientHttpRequestFactory criarRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);
        return factory;
    }
}