package br.com.portfolio.pedidos.api.exception;

public class ServicoExternoException extends RuntimeException {

    public ServicoExternoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}