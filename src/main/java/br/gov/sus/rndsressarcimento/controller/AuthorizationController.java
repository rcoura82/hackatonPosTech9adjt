package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.domain.AuthorizationRecord;
import br.gov.sus.rndsressarcimento.dto.CreateAuthorizationRequest;
import br.gov.sus.rndsressarcimento.service.AuthorizationSmartContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/autorizacoes")
@Tag(name = "Autorizacoes AIH APAC ABI")
public class AuthorizationController {

    private final AuthorizationSmartContract authorizationSmartContract;

    public AuthorizationController(AuthorizationSmartContract authorizationSmartContract) {
        this.authorizationSmartContract = authorizationSmartContract;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar autorizacao AIH APAC ou ABI")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public AuthorizationRecord create(@Valid @RequestBody CreateAuthorizationRequest request) {
        return authorizationSmartContract.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar autorizacoes com filtros")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public List<AuthorizationRecord> list(
            @Parameter(description = "Filtrar por beneficiario") @RequestParam(required = false) String beneficiaryId,
            @Parameter(description = "Filtrar por tipo AIH APAC ABI") @RequestParam(required = false) String type
    ) {
        return authorizationSmartContract.listAll(beneficiaryId, type);
    }
}
