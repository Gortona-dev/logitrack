package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	List<Order> findByClientId(UUID clientId);

	boolean existsByTrackingCode(String trackingCode);

	long countByTrackingCodeStartingWith(String prefix);
}
