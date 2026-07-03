package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

	Optional<Delivery> findByOrderId(UUID orderId);

	boolean existsByOrderId(UUID orderId);

	List<Delivery> findByOrderClientId(UUID clientId);

	List<Delivery> findByDeliveryPersonId(UUID deliveryPersonId);

	@EntityGraph(attributePaths = {"order", "order.client", "deliveryPerson", "vehicle"})
	@Query("""
			select d from Delivery d
			join d.order o
			join o.client c
			left join d.deliveryPerson dp
			left join d.vehicle v
			where (:clientId is null or c.id = :clientId)
				and (:deliveryPersonId is null or dp.id = :deliveryPersonId)
				and (:status is null or d.status = :status)
				and (
					:search is null
					or :search = ''
					or lower(o.trackingCode) like lower(concat('%', :search, '%'))
					or lower(c.name) like lower(concat('%', :search, '%'))
					or lower(o.pickupAddress) like lower(concat('%', :search, '%'))
					or lower(o.deliveryAddress) like lower(concat('%', :search, '%'))
					or lower(o.description) like lower(concat('%', :search, '%'))
				)
			""")
	Page<Delivery> searchOrders(UUID clientId, UUID deliveryPersonId, DeliveryStatus status, String search, Pageable pageable);

	@Query("""
			select d.deliveryPerson.id as deliveryPersonId,
				concat(v.licensePlate, ' - ', v.model) as vehicleLabel
			from Delivery d
			join d.vehicle v
			where d.deliveryPerson.id in :deliveryPersonIds
				and d.status in :statuses
			""")
	List<AssignedVehicleProjection> findAssignedVehiclesByDeliveryPersonIds(List<UUID> deliveryPersonIds, List<DeliveryStatus> statuses);

	List<Delivery> findByStatus(DeliveryStatus status);

	boolean existsByVehicleIdAndStatusIn(UUID vehicleId, List<DeliveryStatus> statuses);

	boolean existsByVehicleIdAndStatusInAndIdNot(UUID vehicleId, List<DeliveryStatus> statuses, UUID id);

	boolean existsByDeliveryPersonIdAndStatusIn(UUID deliveryPersonId, List<DeliveryStatus> statuses);

	boolean existsByDeliveryPersonIdAndStatusInAndIdNot(UUID deliveryPersonId, List<DeliveryStatus> statuses, UUID id);

	long countByStatus(DeliveryStatus status);

	long countByStatusAndUpdatedAtBetween(DeliveryStatus status, java.time.OffsetDateTime start, java.time.OffsetDateTime end);

	List<Delivery> findTop5ByOrderByUpdatedAtDesc();

	interface AssignedVehicleProjection {
		UUID getDeliveryPersonId();

		String getVehicleLabel();
	}
}
