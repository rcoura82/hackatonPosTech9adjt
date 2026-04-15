package br.gov.sus.rndsressarcimento.persistence.repository;

import br.gov.sus.rndsressarcimento.persistence.entity.ReimbursementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReimbursementRepository extends JpaRepository<ReimbursementEntity, String> {
    List<ReimbursementEntity> findByOperatorId(String operatorId);
}
