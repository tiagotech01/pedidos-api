package br.com.portfolio.pedidos.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.portfolio.pedidos.api.dto.ClienteRequest;
import br.com.portfolio.pedidos.api.dto.ClienteResponse;
import br.com.portfolio.pedidos.api.dto.ClienteResponse.EnderecoResponse;
import br.com.portfolio.pedidos.api.exception.ServicoExternoException;
import br.com.portfolio.pedidos.api.service.ClienteService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String JSON_VALIDO = """
            {"nome": "Maria Silva", "email": "maria@email.com", "cep": "01001-000", "numero": "100"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    private final ClienteResponse maria = new ClienteResponse(1L, "Maria Silva", "maria@email.com",
            new EnderecoResponse("01001000", "Praca da Se", "100", null, "Se", "Sao Paulo", "SP"));

    @Test
    @DisplayName("POST deve criar o cliente e devolver 201")
    void deveCriarCliente() throws Exception {
        when(service.criar(any(ClienteRequest.class))).thenReturn(maria);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endereco.uf").value("SP"));
    }

    @Test
    @DisplayName("POST deve recusar CEP fora do formato")
    void deveRecusarCepInvalido() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria", "email": "maria@email.com", "cep": "123", "numero": "1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cep").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST deve devolver 503 quando o ViaCEP está indisponível")
    void deveDevolver503QuandoViaCepFalha() throws Exception {
        when(service.criar(any(ClienteRequest.class)))
                .thenThrow(new ServicoExternoException("Não foi possível consultar o CEP no ViaCEP", null));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("GET deve listar e buscar clientes")
    void deveListarEBuscarClientes() throws Exception {
        when(service.listar()).thenReturn(List.of(maria));
        when(service.buscarPorId(1L)).thenReturn(maria);

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("maria@email.com"));
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }
}
