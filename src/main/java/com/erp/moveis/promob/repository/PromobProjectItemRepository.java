package com.erp.moveis.promob.repository;

import com.erp.moveis.promob.entity.PromobProjectItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromobProjectItemRepository extends JpaRepository<PromobProjectItem, Long> {

    List<PromobProjectItem> findByProjectId(Long projectId);
}
