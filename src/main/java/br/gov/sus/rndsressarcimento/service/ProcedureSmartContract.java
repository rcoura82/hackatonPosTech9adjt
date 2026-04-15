package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import br.gov.sus.rndsressarcimento.domain.ProcedureBase;
import br.gov.sus.rndsressarcimento.dto.CreateProcedureRequest;
import br.gov.sus.rndsressarcimento.exception.NotFoundException;
import br.gov.sus.rndsressarcimento.persistence.entity.ProcedureEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.ProcedureRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProcedureSmartContract {

    private final ProcedureRepository procedureRepository;
    private final BlockchainService blockchainService;

    public ProcedureSmartContract(ProcedureRepository procedureRepository, BlockchainService blockchainService) {
        this.procedureRepository = procedureRepository;
        this.blockchainService = blockchainService;
    }

    public ProcedureBase create(CreateProcedureRequest request) {
        ProcedureEntity entity = new ProcedureEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTussCode(request.tussCode());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setBaseCost(request.baseCost());
        entity.setCreatedAt(Instant.now());
        procedureRepository.save(entity);

        ProcedureBase procedure = toDomain(entity);
        blockchainService.appendTransaction("ProcedureSmartContract", "CREATE_PROCEDURE", Map.of(
                "procedureId", procedure.id(),
                "tussCode", procedure.tussCode(),
                "type", procedure.type().name(),
                "baseCost", procedure.baseCost()
        ));
        return procedure;
    }

    public ProcedureBase getById(String id) {
        return procedureRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new NotFoundException("Procedimento nao encontrado: " + id));
    }

    public List<ProcedureBase> listAll() {
        return procedureRepository.findAll().stream().map(this::toDomain).toList();
    }

    private ProcedureBase toDomain(ProcedureEntity entity) {
        return new ProcedureBase(
                entity.getId(),
                entity.getTussCode(),
                entity.getDescription(),
                entity.getType(),
                entity.getBaseCost(),
                entity.getCreatedAt()
        );
    }
}
