package br.gov.sus.rndsressarcimento.domain;

import java.time.Instant;

public record Operator(
        String id,
        String ansCode,
        String corporateName,
        String cnpj,
        Instant createdAt
) {
}
