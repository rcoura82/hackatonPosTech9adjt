package br.gov.sus.rndsressarcimento.blockchain;

import java.time.Instant;
import java.util.List;

public record Block(
        int index,
        Instant timestamp,
        String previousHash,
        String hash,
        List<BlockchainTransaction> transactions
) {
}
