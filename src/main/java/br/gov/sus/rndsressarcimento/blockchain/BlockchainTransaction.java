package br.gov.sus.rndsressarcimento.blockchain;

import java.time.Instant;
import java.util.Map;

public record BlockchainTransaction(
        String id,
        String smartContract,
        String operation,
        Instant timestamp,
        Map<String, Object> payload
) {
}
