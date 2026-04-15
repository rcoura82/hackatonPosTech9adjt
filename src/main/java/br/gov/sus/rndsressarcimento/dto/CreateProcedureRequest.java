package br.gov.sus.rndsressarcimento.dto;

import br.gov.sus.rndsressarcimento.domain.AuthorizationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProcedureRequest(
        @NotBlank String tussCode,
        @NotBlank String description,
        @NotNull AuthorizationType type,
        @NotNull @DecimalMin("0.0") BigDecimal baseCost
) {
}
