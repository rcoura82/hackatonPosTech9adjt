package br.gov.sus.rndsressarcimento.dto;

import java.math.BigDecimal;

public record ProcedureAnalyticsResponse(
        String procedureId,
        String procedureDescription,
        long reimbursedCount,
        BigDecimal totalReimbursed
) {
}
