package br.com.portfolio.pedidos.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProdutoTest {

    private Produto novoProduto(int estoque) {
        return new Produto("Teclado", "Teclado mecânico", new BigDecimal("199.90"), estoque);
    }

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("deve criar produto com dados válidos")
        void deveCriarProdutoValido() {
            Produto produto = novoProduto(10);

            assertThat(produto.getNome()).isEqualTo("Teclado");
            assertThat(produto.getPreco()).isEqualByComparingTo("199.90");
            assertThat(produto.getEstoque()).isEqualTo(10);
            assertThat(produto.isAtivo()).isTrue();
            assertThat(produto.getCriadoEm()).isNotNull();
        }

        @Test
        @DisplayName("não deve criar produto com preço zero")
        void naoDeveCriarComPrecoZero() {
            assertThatThrownBy(() -> new Produto("Teclado", null, BigDecimal.ZERO, 1))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("O preço deve ser maior que zero");
        }

        @ParameterizedTest(name = "nome inválido: \"{0}\"")
        @ValueSource(strings = {"", "   "})
        @DisplayName("não deve criar produto sem nome")
        void naoDeveCriarSemNome(String nome) {
            assertThatThrownBy(() -> new Produto(nome, null, BigDecimal.TEN, 1))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("O nome do produto é obrigatório");
        }

        @Test
        @DisplayName("não deve criar produto com estoque negativo")
        void naoDeveCriarComEstoqueNegativo() {
            assertThatThrownBy(() -> novoProduto(-1))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessage("O estoque inicial não pode ser negativo");
        }
    }

    @Nested
    @DisplayName("Estoque")
    class Estoque {

        @Test
        @DisplayName("deve baixar o estoque")
        void deveBaixarEstoque() {
            Produto produto = novoProduto(10);

            produto.baixarEstoque(3);

            assertThat(produto.getEstoque()).isEqualTo(7);
        }

        @Test
        @DisplayName("não deve baixar mais do que o disponível")
        void naoDeveBaixarMaisQueDisponivel() {
            Produto produto = novoProduto(2);

            assertThatThrownBy(() -> produto.baixarEstoque(5))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Estoque insuficiente");
            assertThat(produto.getEstoque()).isEqualTo(2);
        }

        @ParameterizedTest(name = "quantidade inválida: {0}")
        @ValueSource(ints = {0, -1})
        @DisplayName("não deve aceitar quantidade zero ou negativa")
        void naoDeveAceitarQuantidadeInvalida(int quantidade) {
            Produto produto = novoProduto(10);

            assertThatThrownBy(() -> produto.baixarEstoque(quantidade))
                    .isInstanceOf(RegraNegocioException.class);
        }

        @Test
        @DisplayName("deve repor o estoque")
        void deveReporEstoque() {
            Produto produto = novoProduto(1);

            produto.reporEstoque(4);

            assertThat(produto.getEstoque()).isEqualTo(5);
        }
    }
}