package br.com.portfolio.pedidos.api.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super("%s %d não encontrado".formatted(recurso, id));
    }
}