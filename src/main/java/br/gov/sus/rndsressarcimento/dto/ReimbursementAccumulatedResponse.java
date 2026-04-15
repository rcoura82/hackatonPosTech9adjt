package br.gov.sus.rndsressarcimento.dto;

import java.math.BigDecimal;

public record ReimbursementAccumulatedResponse(
        String period,
        BigDecimal total
) {
}
