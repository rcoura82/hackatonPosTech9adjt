package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.domain.Operator;
import br.gov.sus.rndsressarcimento.dto.CreateOperatorRequest;
import br.gov.sus.rndsressarcimento.service.OperatorSmartContract;
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
@RequestMapping("/api/v1/operadoras")
@Tag(name = "Operadoras")
public class OperatorController {

    private final OperatorSmartContract operatorSmartContract;

    public OperatorController(OperatorSmartContract operatorSmartContract) {
        this.operatorSmartContract = operatorSmartContract;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar operadora")
    @PreAuthorize("hasRole('SUS')")
    public Operator create(@Valid @RequestBody CreateOperatorRequest request) {
        return operatorSmartContract.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar operadoras")
    @PreAuthorize("hasAnyRole('SUS','OPERADORA')")
    public List<Operator> list() {
        return operatorSmartContract.listAll();
    }
}
