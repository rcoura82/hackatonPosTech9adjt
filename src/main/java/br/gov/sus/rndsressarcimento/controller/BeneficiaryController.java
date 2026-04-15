package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.domain.Beneficiary;
import br.gov.sus.rndsressarcimento.dto.CreateBeneficiaryRequest;
import br.gov.sus.rndsressarcimento.service.BeneficiarySmartContract;
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
@RequestMapping("/api/v1/beneficiarios")
@Tag(name = "Beneficiarios")
public class BeneficiaryController {

    private final BeneficiarySmartContract beneficiarySmartContract;

    public BeneficiaryController(BeneficiarySmartContract beneficiarySmartContract) {
        this.beneficiarySmartContract = beneficiarySmartContract;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar beneficiario")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public Beneficiary create(@Valid @RequestBody CreateBeneficiaryRequest request) {
        return beneficiarySmartContract.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar beneficiarios")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public List<Beneficiary> list() {
        return beneficiarySmartContract.listAll();
    }
}
