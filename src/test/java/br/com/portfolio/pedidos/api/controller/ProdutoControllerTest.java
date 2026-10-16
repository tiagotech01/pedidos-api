package br.com.portfolio.pedidos.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.portfolio.pedidos.api.dto.ProdutoRequest;
import br.com.portfolio.pedidos.api.dto.ProdutoResponse;
import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.service.ProdutoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    private static final String JSON_VALIDO = """
            {"nome": "Teclado", "descricao": "Mecanico", "preco": 199.90, "estoque": 10}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService service;

    private final ProdutoResponse teclado =
            new ProdutoResponse(1L, "Teclado", "Mecanico", new BigDecimal("199.90"), 10, true);

    @Test
    @DisplayName("POST deve criar o produto e devolver 201 com o header Location")
    void deveCriarProduto() throws Exception {
        when(service.criar(any(ProdutoRequest.class))).thenReturn(teclado);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/produtos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Teclado"));
    }

    @Test
    @DisplayName("POST deve devolver 400 com os campos inválidos e não chamar o service")
    void deveRecusarCamposInvalidos() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "preco": -5, "estoque": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.preco").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST deve devolver 400 quando o JSON está malformado")
    void deveRecusarJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST deve devolver 422 quando uma regra de negócio é violada")
    void deveDevolver422EmRegraDeNegocio() throws Exception {
        when(service.criar(any(ProdutoRequest.class)))
                .thenThrow(new RegraNegocioException("Já existe um produto com o nome 'Teclado'"));

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("GET deve listar os produtos ativos")
    void deveListarProdutos() throws Exception {
        when(service.listarAtivos()).thenReturn(List.of(teclado));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Teclado"));
    }

    @Test
    @DisplayName("GET deve devolver 404 quando o produto não existe")
    void deveDevolver404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Produto", 99L));

        mockMvc.perform(get("/api/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT deve atualizar o produto")
    void deveAtualizarProduto() throws Exception {
        when(service.atualizar(eq(1L), any(ProdutoRequest.class))).thenReturn(teclado);

        mockMvc.perform(put("/api/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE deve desativar o produto e devolver 204")
    void deveDesativarProduto() throws Exception {
        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());

        verify(service).desativar(1L);
    }
}