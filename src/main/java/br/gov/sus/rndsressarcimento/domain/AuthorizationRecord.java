package br.gov.sus.rndsressarcimento.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record AuthorizationRecord(
        String id,
        String externalCode,
        AuthorizationType type,
        String beneficiaryId,
        String operatorId,
        String procedureId,
        BigDecimal requestedAmount,
        AuthorizationStatus status,
        Instant requestedAt
) {
}
