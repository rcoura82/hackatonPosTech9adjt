package br.gov.sus.rndsressarcimento.domain;

import java.time.Instant;

public record IntegrationConfig(
        String id,
        OrganizationType organizationType,
        String organizationId,
        String endpoint,
        String apiKeyMasked,
        boolean enabled,
        Instant updatedAt
) {
}
