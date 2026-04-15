package br.gov.sus.rndsressarcimento.dto;

import br.gov.sus.rndsressarcimento.domain.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateCredentialRequest(
        @NotNull OrganizationType organizationType,
        @NotBlank String organizationId,
        @NotBlank String username,
        @NotBlank String password,
        @NotEmpty Set<String> roles
) {
}
