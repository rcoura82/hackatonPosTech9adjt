package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import br.gov.sus.rndsressarcimento.domain.AuthorizationStatus;
import br.gov.sus.rndsressarcimento.domain.Reimbursement;
import br.gov.sus.rndsressarcimento.domain.ReimbursementStatus;
import br.gov.sus.rndsressarcimento.dto.CreateReimbursementRequest;
import br.gov.sus.rndsressarcimento.exception.BusinessException;
import br.gov.sus.rndsressarcimento.exception.NotFoundException;
import br.gov.sus.rndsressarcimento.persistence.entity.ReimbursementEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.ReimbursementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReimbursementSmartContract {

    private final ReimbursementRepository reimbursementRepository;
    private final AuthorizationSmartContract authorizationSmartContract;
    private final OperatorSmartContract operatorSmartContract;
    private final BlockchainService blockchainService;

    public ReimbursementSmartContract(
            ReimbursementRepository reimbursementRepository,
            AuthorizationSmartContract authorizationSmartContract,
            OperatorSmartContract operatorSmartContract,
            BlockchainService blockchainService
    ) {
        this.reimbursementRepository = reimbursementRepository;
        this.authorizationSmartContract = authorizationSmartContract;
        this.operatorSmartContract = operatorSmartContract;
        this.blockchainService = blockchainService;
    }

    public Reimbursement create(CreateReimbursementRequest request) {
        var authorization = authorizationSmartContract.getById(request.authorizationId());
        operatorSmartContract.getById(request.operatorId());

        if (authorization.status() != AuthorizationStatus.APROVADA) {
            throw new BusinessException("Somente autorizacoes aprovadas podem ser ressarcidas");
        }
        if (!authorization.operatorId().equals(request.operatorId())) {
            throw new BusinessException("Operadora divergente da autorizacao");
        }

        ReimbursementEntity entity = new ReimbursementEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAuthorizationId(request.authorizationId());
        entity.setOperatorId(request.operatorId());
        entity.setSusUnit(request.susUnit());
        entity.setAmount(request.amount());
        entity.setStatus(ReimbursementStatus.ABERTO);
        entity.setCreatedAt(Instant.now());
        reimbursementRepository.save(entity);

        Reimbursement reimbursement = toDomain(entity);
        blockchainService.appendTransaction("ReimbursementSmartContract", "CREATE_REIMBURSEMENT", Map.of(
                "reimbursementId", reimbursement.id(),
                "authorizationId", reimbursement.authorizationId(),
                "operatorId", reimbursement.operatorId(),
                "amount", reimbursement.amount()
        ));
        return reimbursement;
    }

    public Reimbursement markAsPaid(String reimbursementId) {
        Reimbursement current = getById(reimbursementId);
        if (current.status() == ReimbursementStatus.PAGO) {
            return current;
        }

        ReimbursementEntity entity = reimbursementRepository.findById(reimbursementId)
            .orElseThrow(() -> new NotFoundException("Ressarcimento nao encontrado: " + reimbursementId));
        entity.setStatus(ReimbursementStatus.PAGO);
        entity.setPaidAt(Instant.now());
        reimbursementRepository.save(entity);
        Reimbursement paid = toDomain(entity);

        blockchainService.appendTransaction("ReimbursementSmartContract", "MARK_REIMBURSEMENT_PAID", Map.of(
                "reimbursementId", paid.id(),
                "paidAt", paid.paidAt().toString()
        ));
        return paid;
    }

    public Reimbursement getById(String id) {
        return reimbursementRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new NotFoundException("Ressarcimento nao encontrado: " + id));
    }

    public List<Reimbursement> listAll() {
        return reimbursementRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Reimbursement toDomain(ReimbursementEntity entity) {
        return new Reimbursement(
                entity.getId(),
                entity.getAuthorizationId(),
                entity.getOperatorId(),
                entity.getSusUnit(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }
}
