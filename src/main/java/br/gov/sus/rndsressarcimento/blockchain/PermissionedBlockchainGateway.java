package br.gov.sus.rndsressarcimento.blockchain;

public interface PermissionedBlockchainGateway {
    void publish(Block block, BlockchainTransaction transaction);
}
