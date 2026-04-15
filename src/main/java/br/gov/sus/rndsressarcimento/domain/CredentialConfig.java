package br.gov.sus.rndsressarcimento.domain;

import java.time.Instant;
import java.util.Set;

public record CredentialConfig(
        String id,
        OrganizationType organizationType,
        String organizationId,
        String username,
        String passwordHash,
        Set<String> roles,
        Instant updatedAt
) {
}
