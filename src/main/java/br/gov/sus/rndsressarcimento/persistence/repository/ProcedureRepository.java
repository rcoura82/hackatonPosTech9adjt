package br.gov.sus.rndsressarcimento.persistence.repository;

import br.gov.sus.rndsressarcimento.persistence.entity.ProcedureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureRepository extends JpaRepository<ProcedureEntity, String> {
}
