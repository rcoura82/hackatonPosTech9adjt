package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import br.gov.sus.rndsressarcimento.domain.AuthorizationRecord;
import br.gov.sus.rndsressarcimento.domain.AuthorizationStatus;
import br.gov.sus.rndsressarcimento.domain.ProcedureBase;
import br.gov.sus.rndsressarcimento.dto.CreateAuthorizationRequest;
import br.gov.sus.rndsressarcimento.exception.BusinessException;
import br.gov.sus.rndsressarcimento.exception.NotFoundException;
import br.gov.sus.rndsressarcimento.persistence.entity.AuthorizationEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.AuthorizationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthorizationSmartContract {

    private final AuthorizationRepository authorizationRepository;
    private final BeneficiarySmartContract beneficiarySmartContract;
    private final OperatorSmartContract operatorSmartContract;
    private final ProcedureSmartContract procedureSmartContract;
    private final BlockchainService blockchainService;

    public AuthorizationSmartContract(
            AuthorizationRepository authorizationRepository,
            BeneficiarySmartContract beneficiarySmartContract,
            OperatorSmartContract operatorSmartContract,
            ProcedureSmartContract procedureSmartContract,
            BlockchainService blockchainService
    ) {
        this.authorizationRepository = authorizationRepository;
        this.beneficiarySmartContract = beneficiarySmartContract;
        this.operatorSmartContract = operatorSmartContract;
        this.procedureSmartContract = procedureSmartContract;
        this.blockchainService = blockchainService;
    }

    public AuthorizationRecord create(CreateAuthorizationRequest request) {
        var beneficiary = beneficiarySmartContract.getById(request.beneficiaryId());
        operatorSmartContract.getById(request.operatorId());
        ProcedureBase procedure = procedureSmartContract.getById(request.procedureId());

        if (!beneficiary.operatorId().equals(request.operatorId())) {
            throw new BusinessException("Beneficiario nao pertence a operadora informada");
        }
        if (procedure.type() != request.type()) {
            throw new BusinessException("Tipo da autorizacao diverge do procedimento");
        }

        BigDecimal maxAllowed = procedure.baseCost().multiply(BigDecimal.valueOf(1.5));
        AuthorizationStatus status = request.requestedAmount().compareTo(maxAllowed) <= 0
                ? AuthorizationStatus.APROVADA
                : AuthorizationStatus.NEGADA;

        AuthorizationEntity entity = new AuthorizationEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setExternalCode(request.externalCode());
        entity.setType(request.type());
        entity.setBeneficiaryId(request.beneficiaryId());
        entity.setOperatorId(request.operatorId());
        entity.setProcedureId(request.procedureId());
        entity.setRequestedAmount(request.requestedAmount());
        entity.setStatus(status);
        entity.setRequestedAt(Instant.now());
        authorizationRepository.save(entity);

        AuthorizationRecord authorization = toDomain(entity);
        blockchainService.appendTransaction("AuthorizationSmartContract", "CREATE_AUTHORIZATION", Map.of(
                "authorizationId", authorization.id(),
                "externalCode", authorization.externalCode(),
                "type", authorization.type().name(),
                "status", authorization.status().name(),
                "operatorId", authorization.operatorId(),
                "beneficiaryId", authorization.beneficiaryId()
        ));
        return authorization;
    }

    public AuthorizationRecord getById(String id) {
    return authorizationRepository.findById(id)
        .map(this::toDomain)
        .orElseThrow(() -> new NotFoundException("Autorizacao nao encontrada: " + id));
    }

    public List<AuthorizationRecord> listAll(String beneficiaryId, String type) {
    List<AuthorizationEntity> base = beneficiaryId == null
        ? authorizationRepository.findAll()
        : authorizationRepository.findByBeneficiaryId(beneficiaryId);
    return base.stream()
        .map(this::toDomain)
                .filter(item -> beneficiaryId == null || item.beneficiaryId().equals(beneficiaryId))
                .filter(item -> type == null || item.type().name().equalsIgnoreCase(type))
                .toList();
    }

    private AuthorizationRecord toDomain(AuthorizationEntity entity) {
    return new AuthorizationRecord(
        entity.getId(),
        entity.getExternalCode(),
        entity.getType(),
        entity.getBeneficiaryId(),
        entity.getOperatorId(),
        entity.getProcedureId(),
        entity.getRequestedAmount(),
        entity.getStatus(),
        entity.getRequestedAt()
    );
    }
}
