package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.dto.CostSummaryResponse;
import br.gov.sus.rndsressarcimento.service.CostSmartContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/custos")
@Tag(name = "Custos")
public class CostController {

    private final CostSmartContract costSmartContract;

    public CostController(CostSmartContract costSmartContract) {
        this.costSmartContract = costSmartContract;
    }

    @GetMapping("/beneficiarios/{id}")
    @Operation(summary = "Consultar custos por beneficiario")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public CostSummaryResponse byBeneficiary(@PathVariable String id) {
        return costSmartContract.summarizeByBeneficiary(id);
    }

    @GetMapping("/operadoras/{id}")
    @Operation(summary = "Consultar custos por operadora")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public CostSummaryResponse byOperator(@PathVariable String id) {
        return costSmartContract.summarizeByOperator(id);
    }
}
