package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import br.gov.sus.rndsressarcimento.domain.Beneficiary;
import br.gov.sus.rndsressarcimento.dto.CreateBeneficiaryRequest;
import br.gov.sus.rndsressarcimento.exception.BusinessException;
import br.gov.sus.rndsressarcimento.exception.NotFoundException;
import br.gov.sus.rndsressarcimento.persistence.entity.BeneficiaryEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BeneficiarySmartContract {

    private final BeneficiaryRepository beneficiaryRepository;
    private final OperatorSmartContract operatorSmartContract;
    private final BlockchainService blockchainService;

    public BeneficiarySmartContract(
            BeneficiaryRepository beneficiaryRepository,
            OperatorSmartContract operatorSmartContract,
            BlockchainService blockchainService
    ) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.operatorSmartContract = operatorSmartContract;
        this.blockchainService = blockchainService;
    }

    public Beneficiary create(CreateBeneficiaryRequest request) {
        operatorSmartContract.getById(request.operatorId());

        boolean duplicateDocument = beneficiaryRepository.existsByDocument(request.document());
        if (duplicateDocument) {
            throw new BusinessException("Documento de beneficiario ja cadastrado");
        }

        BeneficiaryEntity entity = new BeneficiaryEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setDocument(request.document());
        entity.setName(request.name());
        entity.setOperatorId(request.operatorId());
        entity.setPlanNumber(request.planNumber());
        entity.setCreatedAt(Instant.now());
        beneficiaryRepository.save(entity);

        Beneficiary beneficiary = toDomain(entity);
        blockchainService.appendTransaction("BeneficiarySmartContract", "CREATE_BENEFICIARY", Map.of(
                "beneficiaryId", beneficiary.id(),
                "document", beneficiary.document(),
                "operatorId", beneficiary.operatorId()
        ));
        return beneficiary;
    }

    public Beneficiary getById(String id) {
        return beneficiaryRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new NotFoundException("Beneficiario nao encontrado: " + id));
    }

    public List<Beneficiary> listAll() {
        return beneficiaryRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Beneficiary toDomain(BeneficiaryEntity entity) {
        return new Beneficiary(
                entity.getId(),
                entity.getDocument(),
                entity.getName(),
                entity.getOperatorId(),
                entity.getPlanNumber(),
                entity.getCreatedAt()
        );
    }
}
