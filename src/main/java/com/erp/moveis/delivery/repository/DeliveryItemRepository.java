package com.erp.moveis.delivery.repository;

import com.erp.moveis.delivery.entity.DeliveryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, Long> {

    List<DeliveryItem> findByDeliveryId(Long deliveryId);
}
