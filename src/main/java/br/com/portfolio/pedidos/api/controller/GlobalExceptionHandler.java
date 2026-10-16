package br.com.portfolio.pedidos.api.controller;

import br.com.portfolio.pedidos.api.exception.RecursoNaoEncontradoException;
import br.com.portfolio.pedidos.api.exception.RegraNegocioException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import br.com.portfolio.pedidos.api.exception.ServicoExternoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int UNPROCESSABLE = 422;

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail tratarRegraNegocio(RegraNegocioException ex) {
        return problema(
                HttpStatusCode.valueOf(UNPROCESSABLE), "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(ServicoExternoException.class)
    public ProblemDetail tratarServicoExterno(ServicoExternoException ex) {
        return problema(
                HttpStatus.SERVICE_UNAVAILABLE, "Serviço externo indisponível", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = problema(
                HttpStatus.BAD_REQUEST, "Dados inválidos", "Um ou mais campos estão inválidos");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return problema(
                HttpStatus.BAD_REQUEST, "Requisição inválida", "O corpo da requisição não é um JSON válido");
    }

    private ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
