package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.domain.Reimbursement;
import br.gov.sus.rndsressarcimento.dto.CreateReimbursementRequest;
import br.gov.sus.rndsressarcimento.service.ReimbursementSmartContract;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ressarcimentos")
@Tag(name = "Ressarcimentos")
public class ReimbursementController {

    private final ReimbursementSmartContract reimbursementSmartContract;

    public ReimbursementController(ReimbursementSmartContract reimbursementSmartContract) {
        this.reimbursementSmartContract = reimbursementSmartContract;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gerar ressarcimento")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public Reimbursement create(@Valid @RequestBody CreateReimbursementRequest request) {
        return reimbursementSmartContract.create(request);
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Marcar ressarcimento como pago")
    @PreAuthorize("hasRole('SUS')")
    public Reimbursement markAsPaid(@PathVariable String id) {
        return reimbursementSmartContract.markAsPaid(id);
    }

    @GetMapping
    @Operation(summary = "Listar ressarcimentos")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public List<Reimbursement> list() {
        return reimbursementSmartContract.listAll();
    }
}
