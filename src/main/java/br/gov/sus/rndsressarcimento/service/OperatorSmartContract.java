package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.blockchain.BlockchainService;
import br.gov.sus.rndsressarcimento.domain.Operator;
import br.gov.sus.rndsressarcimento.dto.CreateOperatorRequest;
import br.gov.sus.rndsressarcimento.exception.BusinessException;
import br.gov.sus.rndsressarcimento.exception.NotFoundException;
import br.gov.sus.rndsressarcimento.persistence.entity.OperatorEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.OperatorRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OperatorSmartContract {

    private final OperatorRepository operatorRepository;
    private final BlockchainService blockchainService;

    public OperatorSmartContract(OperatorRepository operatorRepository, BlockchainService blockchainService) {
        this.operatorRepository = operatorRepository;
        this.blockchainService = blockchainService;
    }

    public Operator create(CreateOperatorRequest request) {
        boolean duplicateCnpj = operatorRepository.existsByCnpj(request.cnpj());
        if (duplicateCnpj) {
            throw new BusinessException("CNPJ de operadora ja cadastrado");
        }

        OperatorEntity entity = new OperatorEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAnsCode(request.ansCode());
        entity.setCorporateName(request.corporateName());
        entity.setCnpj(request.cnpj());
        entity.setCreatedAt(Instant.now());

        operatorRepository.save(entity);
        Operator operator = toDomain(entity);
        blockchainService.appendTransaction("OperatorSmartContract", "CREATE_OPERATOR", Map.of(
                "operatorId", operator.id(),
                "cnpj", operator.cnpj(),
                "ansCode", operator.ansCode()
        ));
        return operator;
    }

    public Operator getById(String id) {
        return operatorRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new NotFoundException("Operadora nao encontrada: " + id));
    }

    public List<Operator> listAll() {
        return operatorRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Operator toDomain(OperatorEntity entity) {
        return new Operator(
                entity.getId(),
                entity.getAnsCode(),
                entity.getCorporateName(),
                entity.getCnpj(),
                entity.getCreatedAt()
        );
    }
}
