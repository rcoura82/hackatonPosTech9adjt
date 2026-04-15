package br.gov.sus.rndsressarcimento.persistence.repository;

import br.gov.sus.rndsressarcimento.domain.OrganizationType;
import br.gov.sus.rndsressarcimento.persistence.entity.IntegrationConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfigEntity, String> {
    Optional<IntegrationConfigEntity> findByOrganizationTypeAndOrganizationId(
            OrganizationType organizationType,
            String organizationId
    );
}
