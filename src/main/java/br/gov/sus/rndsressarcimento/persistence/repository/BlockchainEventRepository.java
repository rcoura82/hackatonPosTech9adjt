package br.gov.sus.rndsressarcimento.persistence.repository;

import br.gov.sus.rndsressarcimento.persistence.entity.BlockchainEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockchainEventRepository extends JpaRepository<BlockchainEventEntity, String> {
}
