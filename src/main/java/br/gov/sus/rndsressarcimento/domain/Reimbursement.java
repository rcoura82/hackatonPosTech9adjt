package br.gov.sus.rndsressarcimento.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Reimbursement(
        String id,
        String authorizationId,
        String operatorId,
        String susUnit,
        BigDecimal amount,
        ReimbursementStatus status,
        Instant createdAt,
        Instant paidAt
) {
}
