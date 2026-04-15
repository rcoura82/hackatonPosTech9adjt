package br.gov.sus.rndsressarcimento.controller;

import br.gov.sus.rndsressarcimento.dto.ProcedureAnalyticsResponse;
import br.gov.sus.rndsressarcimento.dto.ReimbursementAccumulatedResponse;
import br.gov.sus.rndsressarcimento.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sus/relatorios")
@Tag(name = "Relatorios SUS")
public class SusReportController {

    private final ReportService reportService;

    public SusReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/ressarcimento-acumulado")
    @Operation(summary = "Relatorio de ressarcimento acumulado")
    @PreAuthorize("hasRole('SUS')")
    public List<ReimbursementAccumulatedResponse> accumulated() {
        return reportService.accumulatedByMonth();
    }

    @GetMapping("/analise-procedimentos")
    @Operation(summary = "Analise de procedimentos mais ressarcidos")
    @PreAuthorize("hasRole('SUS')")
    public List<ProcedureAnalyticsResponse> analytics() {
        return reportService.procedureAnalytics();
    }

    @GetMapping("/exportar")
    @Operation(summary = "Exportar relatorios financeiros")
    @PreAuthorize("hasRole('SUS')")
    public ResponseEntity<?> export(
            @Parameter(description = "Formato de exportacao: csv ou json")
            @RequestParam(defaultValue = "csv") String formato
    ) {
        if ("json".equalsIgnoreCase(formato)) {
            return ResponseEntity.ok(reportService.accumulatedByMonth());
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(reportService.exportCsv());
    }
}
