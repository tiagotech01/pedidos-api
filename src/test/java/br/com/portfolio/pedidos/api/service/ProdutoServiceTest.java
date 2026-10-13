package br.com.portfolio.pedidos.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.portfolio.pedidos.api.domain.Produto;
import br.com.portfolio.pedidos.api.dto.ProdutoRequest;
import br.com.portfolio.pedidos.api.dto.ProdutoResponse;
import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import br.com.portfolio.pedidos.api.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    private final ProdutoRequest request =
            new ProdutoRequest("Teclado", "Mecânico", new BigDecimal("199.90"), 10);

    private Produto produto(String nome) {
        return new Produto(nome, null, new BigDecimal("100.00"), 5);
    }

    @Test
    @DisplayName("deve criar produto quando o nome não existe")
    void deveCriarProduto() {
        when(repository.existsByNomeIgnoreCase("Teclado")).thenReturn(false);
        when(repository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProdutoResponse response = service.criar(request);

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Teclado");
        assertThat(captor.getValue().getEstoque()).isEqualTo(10);
        assertThat(response.preco()).isEqualByComparingTo("199.90");
    }

    @Test
    @DisplayName("não deve criar produto com nome duplicado")
    void naoDeveCriarComNomeDuplicado() {
        when(repository.existsByNomeIgnoreCase("Teclado")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Já existe um produto");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar exceção quando o produto não existe")
    void deveLancarQuandoNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Produto 99 não encontrado");
    }

    @Test
    @DisplayName("deve atualizar sem chamar save (dirty checking)")
    void deveAtualizarProduto() {
        Produto existente = produto("Teclado");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        ProdutoRequest novosDados =
                new ProdutoRequest("Teclado RGB", "Com luz", new BigDecimal("249.90"), 0);

        ProdutoResponse response = service.atualizar(1L, novosDados);

        assertThat(response.nome()).isEqualTo("Teclado RGB");
        assertThat(existente.getPreco()).isEqualByComparingTo("249.90");
        assertThat(existente.getEstoque()).isEqualTo(5);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve desativar o produto")
    void deveDesativarProduto() {
        Produto existente = produto("Mouse");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.desativar(1L);

        assertThat(existente.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("deve listar apenas produtos ativos convertidos em DTO")
    void deveListarAtivos() {
        when(repository.findByAtivoTrueOrderByNomeAsc())
                .thenReturn(List.of(produto("Mouse"), produto("Teclado")));

        List<ProdutoResponse> resultado = service.listarAtivos();

        assertThat(resultado).extracting(ProdutoResponse::nome).containsExactly("Mouse", "Teclado");
    }
}