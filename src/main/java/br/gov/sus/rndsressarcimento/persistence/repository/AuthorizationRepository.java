package br.gov.sus.rndsressarcimento.persistence.repository;

import br.gov.sus.rndsressarcimento.persistence.entity.AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorizationRepository extends JpaRepository<AuthorizationEntity, String> {
    List<AuthorizationEntity> findByBeneficiaryId(String beneficiaryId);
}
