package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

	Optional<Delivery> findByOrderId(UUID orderId);

	boolean existsByOrderId(UUID orderId);

	List<Delivery> findByOrderClientId(UUID clientId);

	List<Delivery> findByDeliveryPersonId(UUID deliveryPersonId);

	@EntityGraph(attributePaths = "vehicle")
	List<Delivery> findByDeliveryPersonIdInAndStatusIn(List<UUID> deliveryPersonIds, List<DeliveryStatus> statuses);

	List<Delivery> findByStatus(DeliveryStatus status);

	boolean existsByVehicleIdAndStatusIn(UUID vehicleId, List<DeliveryStatus> statuses);

	boolean existsByVehicleIdAndStatusInAndIdNot(UUID vehicleId, List<DeliveryStatus> statuses, UUID id);

	boolean existsByDeliveryPersonIdAndStatusIn(UUID deliveryPersonId, List<DeliveryStatus> statuses);

	boolean existsByDeliveryPersonIdAndStatusInAndIdNot(UUID deliveryPersonId, List<DeliveryStatus> statuses, UUID id);

	long countByStatus(DeliveryStatus status);

	long countByStatusAndUpdatedAtBetween(DeliveryStatus status, java.time.OffsetDateTime start, java.time.OffsetDateTime end);

	List<Delivery> findTop5ByOrderByUpdatedAtDesc();
}
