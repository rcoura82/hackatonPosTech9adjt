package br.gov.sus.rndsressarcimento.dto;

import br.gov.sus.rndsressarcimento.domain.AuthorizationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAuthorizationRequest(
        @NotBlank String externalCode,
        @NotNull AuthorizationType type,
        @NotBlank String beneficiaryId,
        @NotBlank String operatorId,
        @NotBlank String procedureId,
        @NotNull @DecimalMin("0.0") BigDecimal requestedAmount
) {
}
