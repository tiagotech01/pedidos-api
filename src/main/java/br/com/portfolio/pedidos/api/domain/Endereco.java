package br.com.portfolio.pedidos.api.domain;

import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Endereco {

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(length = 200)
    private String logradouro;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    protected Endereco() {
        // construtor exigido pelo JPA
    }

    public Endereco(String cep, String logradouro, String numero, String complemento,
            String bairro, String cidade, String uf) {
        String cepDigitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (cepDigitos.length() != 8) {
            throw new RegraNegocioException("O CEP deve conter 8 dígitos");
        }
        if (numero == null || numero.isBlank()) {
            throw new RegraNegocioException("O número do endereço é obrigatório");
        }
        this.cep = cepDigitos;
        this.logradouro = logradouro;
        this.numero = numero.trim();
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }
}
