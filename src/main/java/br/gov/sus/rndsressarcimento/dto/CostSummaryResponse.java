package br.gov.sus.rndsressarcimento.dto;

import java.math.BigDecimal;

public record CostSummaryResponse(
        String referenceId,
        BigDecimal total,
        long events
) {
}
