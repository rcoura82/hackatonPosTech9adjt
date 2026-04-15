package br.gov.sus.rndsressarcimento.domain;

import java.time.Instant;

public record Beneficiary(
        String id,
        String document,
        String name,
        String operatorId,
        String planNumber,
        Instant createdAt
) {
}
