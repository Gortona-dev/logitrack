package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.DeliveryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, UUID> {

	List<DeliveryStatusHistory> findByDeliveryIdOrderByChangedAtAsc(UUID deliveryId);
}
