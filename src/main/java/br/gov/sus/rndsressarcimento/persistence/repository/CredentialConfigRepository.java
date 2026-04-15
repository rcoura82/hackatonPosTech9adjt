package br.gov.sus.rndsressarcimento.persistence.repository;

import br.gov.sus.rndsressarcimento.domain.OrganizationType;
import br.gov.sus.rndsressarcimento.persistence.entity.CredentialConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialConfigRepository extends JpaRepository<CredentialConfigEntity, String> {
    Optional<CredentialConfigEntity> findByOrganizationTypeAndOrganizationIdAndUsername(
            OrganizationType organizationType,
            String organizationId,
            String username
    );
}
