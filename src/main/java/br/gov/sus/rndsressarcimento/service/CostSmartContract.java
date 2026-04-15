package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.domain.AuthorizationRecord;
import br.gov.sus.rndsressarcimento.domain.Reimbursement;
import br.gov.sus.rndsressarcimento.dto.CostSummaryResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CostSmartContract {

    private final AuthorizationSmartContract authorizationSmartContract;
    private final ReimbursementSmartContract reimbursementSmartContract;
    private final BeneficiarySmartContract beneficiarySmartContract;
    private final OperatorSmartContract operatorSmartContract;

    public CostSmartContract(
            AuthorizationSmartContract authorizationSmartContract,
            ReimbursementSmartContract reimbursementSmartContract,
            BeneficiarySmartContract beneficiarySmartContract,
            OperatorSmartContract operatorSmartContract
    ) {
        this.authorizationSmartContract = authorizationSmartContract;
        this.reimbursementSmartContract = reimbursementSmartContract;
        this.beneficiarySmartContract = beneficiarySmartContract;
        this.operatorSmartContract = operatorSmartContract;
    }

    public CostSummaryResponse summarizeByBeneficiary(String beneficiaryId) {
        beneficiarySmartContract.getById(beneficiaryId);
        var authorizations = authorizationSmartContract.listAll(beneficiaryId, null);
        BigDecimal total = authorizations.stream()
                .map(AuthorizationRecord::requestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CostSummaryResponse(beneficiaryId, total, authorizations.size());
    }

    public CostSummaryResponse summarizeByOperator(String operatorId) {
        operatorSmartContract.getById(operatorId);
        var reimbursements = reimbursementSmartContract.listAll().stream()
                .filter(item -> item.operatorId().equals(operatorId))
                .toList();
        if (reimbursements.isEmpty()) {
            return new CostSummaryResponse(operatorId, BigDecimal.ZERO, 0);
        }

        BigDecimal total = reimbursements.stream()
                .map(Reimbursement::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CostSummaryResponse(operatorId, total, reimbursements.size());
    }
}
