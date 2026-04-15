package br.gov.sus.rndsressarcimento.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBeneficiaryRequest(
        @NotBlank String document,
        @NotBlank String name,
        @NotBlank String operatorId,
        @NotBlank String planNumber
) {
}
