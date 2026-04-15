package br.gov.sus.rndsressarcimento.dto;

import br.gov.sus.rndsressarcimento.domain.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateIntegrationRequest(
        @NotNull OrganizationType organizationType,
        @NotBlank String organizationId,
        @NotBlank String endpoint,
        @NotBlank String apiKey,
        boolean enabled
) {
}
