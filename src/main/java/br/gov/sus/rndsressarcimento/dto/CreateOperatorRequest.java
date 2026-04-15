package br.gov.sus.rndsressarcimento.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOperatorRequest(
        @NotBlank String ansCode,
        @NotBlank String corporateName,
        @NotBlank String cnpj
) {
}
