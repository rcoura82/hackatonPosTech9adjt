package br.gov.sus.rndsressarcimento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateReimbursementRequest(
        @NotBlank String authorizationId,
        @NotBlank String operatorId,
        @NotBlank String susUnit,
        @NotNull @DecimalMin("0.0") BigDecimal amount
) {
}
