package com.erp.moveis.promob.repository;

import com.erp.moveis.promob.entity.PromobCutlistPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromobCutlistPartRepository extends JpaRepository<PromobCutlistPart, Long> {

    List<PromobCutlistPart> findByProjectId(Long projectId);
}
