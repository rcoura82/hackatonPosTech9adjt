package br.gov.sus.rndsressarcimento.service;

import br.gov.sus.rndsressarcimento.domain.Reimbursement;
import br.gov.sus.rndsressarcimento.dto.ProcedureAnalyticsResponse;
import br.gov.sus.rndsressarcimento.dto.ReimbursementAccumulatedResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReimbursementSmartContract reimbursementSmartContract;
    private final AuthorizationSmartContract authorizationSmartContract;
    private final ProcedureSmartContract procedureSmartContract;

    public ReportService(
            ReimbursementSmartContract reimbursementSmartContract,
            AuthorizationSmartContract authorizationSmartContract,
            ProcedureSmartContract procedureSmartContract
    ) {
        this.reimbursementSmartContract = reimbursementSmartContract;
        this.authorizationSmartContract = authorizationSmartContract;
        this.procedureSmartContract = procedureSmartContract;
    }

    public List<ReimbursementAccumulatedResponse> accumulatedByMonth() {
        Map<String, BigDecimal> grouped = new LinkedHashMap<>();
        for (Reimbursement reimbursement : reimbursementSmartContract.listAll()) {
            String month = YEAR_MONTH.format(reimbursement.createdAt().atZone(ZoneOffset.UTC));
            grouped.merge(month, reimbursement.amount(), BigDecimal::add);
        }
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ReimbursementAccumulatedResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<ProcedureAnalyticsResponse> procedureAnalytics() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, Long> counts = new LinkedHashMap<>();

        for (Reimbursement reimbursement : reimbursementSmartContract.listAll()) {
            var authorization = authorizationSmartContract.getById(reimbursement.authorizationId());
            totals.merge(authorization.procedureId(), reimbursement.amount(), BigDecimal::add);
            counts.merge(authorization.procedureId(), 1L, Long::sum);
        }

        return totals.entrySet().stream()
                .map(entry -> {
                    var procedure = procedureSmartContract.getById(entry.getKey());
                    return new ProcedureAnalyticsResponse(
                            entry.getKey(),
                            procedure.description(),
                            counts.getOrDefault(entry.getKey(), 0L),
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(ProcedureAnalyticsResponse::totalReimbursed).reversed())
                .toList();
    }

    public String exportCsv() {
        List<ReimbursementAccumulatedResponse> rows = accumulatedByMonth();
        StringBuilder builder = new StringBuilder("periodo,total\n");
        for (ReimbursementAccumulatedResponse row : rows) {
            builder.append(row.period()).append(',').append(row.total()).append('\n');
        }
        return builder.toString();
    }
}
