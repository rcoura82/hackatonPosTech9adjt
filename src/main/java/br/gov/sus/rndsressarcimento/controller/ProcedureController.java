package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.domain.ProcedureBase;
import br.gov.sus.rndsressarcimento.dto.CreateProcedureRequest;
import br.gov.sus.rndsressarcimento.service.ProcedureSmartContract;
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
@RequestMapping("/api/v1/procedimentos")
@Tag(name = "Procedimentos")
public class ProcedureController {

    private final ProcedureSmartContract procedureSmartContract;

    public ProcedureController(ProcedureSmartContract procedureSmartContract) {
        this.procedureSmartContract = procedureSmartContract;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar procedimento na base")
    @PreAuthorize("hasRole('SUS')")
    public ProcedureBase create(@Valid @RequestBody CreateProcedureRequest request) {
        return procedureSmartContract.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar procedimentos")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public List<ProcedureBase> list() {
        return procedureSmartContract.listAll();
    }
}
