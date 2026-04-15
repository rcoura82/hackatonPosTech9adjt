package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.domain.CredentialConfig;
import br.gov.sus.rndsressarcimento.domain.IntegrationConfig;
import br.gov.sus.rndsressarcimento.dto.UpdateCredentialRequest;
import br.gov.sus.rndsressarcimento.dto.UpdateIntegrationRequest;
import br.gov.sus.rndsressarcimento.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuracoes")
@Tag(name = "Configuracoes")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping("/credenciais")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar ou atualizar credenciais")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public CredentialConfig upsertCredential(@Valid @RequestBody UpdateCredentialRequest request) {
        return configurationService.upsertCredential(request);
    }

    @GetMapping("/credenciais")
    @Operation(summary = "Listar credenciais configuradas")
    @PreAuthorize("hasRole('SUS')")
    public List<CredentialConfig> listCredentials() {
        return configurationService.listCredentials();
    }

    @PostMapping("/integracoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar ou atualizar integracao com blockchain")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public IntegrationConfig upsertIntegration(@Valid @RequestBody UpdateIntegrationRequest request) {
        return configurationService.upsertIntegration(request);
    }

    @GetMapping("/integracoes")
    @Operation(summary = "Listar integracoes configuradas")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public List<IntegrationConfig> listIntegrations() {
        return configurationService.listIntegrations();
    }
}
