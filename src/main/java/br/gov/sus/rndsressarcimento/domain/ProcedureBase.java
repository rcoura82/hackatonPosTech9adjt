package br.gov.sus.rndsressarcimento.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ProcedureBase(
        String id,
        String tussCode,
        String description,
        AuthorizationType type,
        BigDecimal baseCost,
        Instant createdAt
) {
}
