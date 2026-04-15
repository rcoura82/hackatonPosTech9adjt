package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import br.gov.sus.rndsressarcimento.domain.CredentialConfig;
import br.gov.sus.rndsressarcimento.domain.IntegrationConfig;
import br.gov.sus.rndsressarcimento.dto.UpdateCredentialRequest;
import br.gov.sus.rndsressarcimento.dto.UpdateIntegrationRequest;
import br.gov.sus.rndsressarcimento.persistence.entity.CredentialConfigEntity;
import br.gov.sus.rndsressarcimento.persistence.entity.IntegrationConfigEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.CredentialConfigRepository;
import br.gov.sus.rndsressarcimento.persistence.repository.IntegrationConfigRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfigurationService {

    private final CredentialConfigRepository credentialConfigRepository;
    private final IntegrationConfigRepository integrationConfigRepository;
    private final BlockchainService blockchainService;

    public ConfigurationService(
        CredentialConfigRepository credentialConfigRepository,
        IntegrationConfigRepository integrationConfigRepository,
        BlockchainService blockchainService
    ) {
    this.credentialConfigRepository = credentialConfigRepository;
    this.integrationConfigRepository = integrationConfigRepository;
        this.blockchainService = blockchainService;
    }

    public CredentialConfig upsertCredential(UpdateCredentialRequest request) {
    CredentialConfigEntity entity = credentialConfigRepository
        .findByOrganizationTypeAndOrganizationIdAndUsername(
            request.organizationType(),
            request.organizationId(),
            request.username()
        )
        .orElseGet(CredentialConfigEntity::new);

    if (entity.getId() == null) {
        entity.setId(UUID.randomUUID().toString());
    }
    entity.setOrganizationType(request.organizationType());
    entity.setOrganizationId(request.organizationId());
    entity.setUsername(request.username());
    entity.setPasswordHash(sha256(request.password()));
    entity.setRoles(request.roles());
    entity.setUpdatedAt(Instant.now());
    credentialConfigRepository.save(entity);

    CredentialConfig config = toDomain(entity);
        blockchainService.appendTransaction("ConfigurationSmartContract", "UPSERT_CREDENTIAL", Map.of(
                "credentialId", config.id(),
                "organizationType", config.organizationType().name(),
                "organizationId", config.organizationId(),
                "username", config.username()
        ));
        return config;
    }

    public IntegrationConfig upsertIntegration(UpdateIntegrationRequest request) {
    IntegrationConfigEntity entity = integrationConfigRepository
        .findByOrganizationTypeAndOrganizationId(request.organizationType(), request.organizationId())
        .orElseGet(IntegrationConfigEntity::new);

    if (entity.getId() == null) {
        entity.setId(UUID.randomUUID().toString());
    }
    entity.setOrganizationType(request.organizationType());
    entity.setOrganizationId(request.organizationId());
    entity.setEndpoint(request.endpoint());
    entity.setApiKeyMasked(mask(request.apiKey()));
    entity.setEnabled(request.enabled());
    entity.setUpdatedAt(Instant.now());
    integrationConfigRepository.save(entity);

    IntegrationConfig config = toDomain(entity);
        blockchainService.appendTransaction("ConfigurationSmartContract", "UPSERT_INTEGRATION", Map.of(
                "integrationId", config.id(),
                "organizationType", config.organizationType().name(),
                "organizationId", config.organizationId(),
                "enabled", config.enabled()
        ));
        return config;
    }

    public List<CredentialConfig> listCredentials() {
        return credentialConfigRepository.findAll().stream().map(this::toDomain).toList();
    }

    public List<IntegrationConfig> listIntegrations() {
        return integrationConfigRepository.findAll().stream().map(this::toDomain).toList();
    }

    private String mask(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    private CredentialConfig toDomain(CredentialConfigEntity entity) {
        return new CredentialConfig(
                entity.getId(),
                entity.getOrganizationType(),
                entity.getOrganizationId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getRoles(),
                entity.getUpdatedAt()
        );
    }

    private IntegrationConfig toDomain(IntegrationConfigEntity entity) {
        return new IntegrationConfig(
                entity.getId(),
                entity.getOrganizationType(),
                entity.getOrganizationId(),
                entity.getEndpoint(),
                entity.getApiKeyMasked(),
                entity.isEnabled(),
                entity.getUpdatedAt()
        );
    }
}
