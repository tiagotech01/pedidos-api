package br.com.portfolio.pedidos.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "deve estar no formato 00000-000")
        String cep,
        @NotBlank @Size(max = 20) String numero,
        @Size(max = 100) String complemento) {
}
