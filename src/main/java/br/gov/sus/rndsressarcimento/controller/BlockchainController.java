package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.blockchain.Block;
import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/blockchain")
@Tag(name = "Blockchain")
public class BlockchainController {

    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/ledger")
    @Operation(summary = "Consultar ledger imutavel")
    @PreAuthorize("hasRole('SUS')")
    public List<Block> ledger() {
        return blockchainService.getLedger();
    }

    @GetMapping("/validacao")
    @Operation(summary = "Validar integridade da cadeia")
    @PreAuthorize("hasRole('SUS')")
    public Map<String, Object> validate() {
        return Map.of("valid", blockchainService.isValid(), "blocks", blockchainService.getLedger().size());
    }
}
