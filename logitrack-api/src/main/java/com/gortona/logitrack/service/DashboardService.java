package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.dashboard.DashboardDeliveryResponse;
import com.gortona.logitrack.dto.dashboard.DashboardDailyDeliveryResponse;
import com.gortona.logitrack.dto.dashboard.DashboardResponse;
import com.gortona.logitrack.dto.dashboard.DashboardStatusCountResponse;
import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.enums.DeliveryPersonStatus;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.enums.VehicleStatus;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.OrderRepository;
import com.gortona.logitrack.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final DeliveryRepository deliveryRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final VehicleRepository vehicleRepository;
	private final OrderRepository orderRepository;

	@Transactional(readOnly = true)
	public DashboardResponse getOverview() {
		ZoneId zone = ZoneId.of("America/Sao_Paulo");
		OffsetDateTime startOfDay = LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();
		OffsetDateTime endOfDay = startOfDay.plusDays(1);
		List<Delivery> deliveries = deliveryRepository.findAll();

		return new DashboardResponse(
				orderRepository.count(),
				deliveries.size(),
				deliveryRepository.countByStatus(DeliveryStatus.PENDING),
				deliveryRepository.countByStatus(DeliveryStatus.ASSIGNED),
				deliveryRepository.countByStatus(DeliveryStatus.PICKED_UP),
				deliveryRepository.countByStatus(DeliveryStatus.IN_TRANSIT),
				deliveryRepository.countByStatus(DeliveryStatus.DELIVERED),
				deliveryRepository.countByStatusAndUpdatedAtBetween(DeliveryStatus.DELIVERED, startOfDay, endOfDay),
				deliveryRepository.countByStatus(DeliveryStatus.CANCELLED),
				deliveryPersonRepository.countByStatus(DeliveryPersonStatus.AVAILABLE),
				deliveryPersonRepository.countByStatus(DeliveryPersonStatus.ON_DELIVERY),
				deliveryPersonRepository.countByStatus(DeliveryPersonStatus.UNAVAILABLE),
				vehicleRepository.countByActiveTrue(),
				vehicleRepository.countByStatusAndActiveTrue(VehicleStatus.AVAILABLE),
				vehicleRepository.countByStatusAndActiveTrue(VehicleStatus.IN_USE),
				vehicleRepository.countByStatusAndActiveTrue(VehicleStatus.MAINTENANCE),
				buildStatusDistribution(),
				buildDeliveredLastSevenDays(deliveries, zone),
				deliveryRepository.findTop5ByOrderByUpdatedAtDesc()
						.stream()
						.map(delivery -> new DashboardDeliveryResponse(
								delivery.getOrder().getId(),
								delivery.getOrder().getClient().getName(),
								delivery.getOrder().getDeliveryAddress(),
								delivery.getStatus(),
								delivery.getUpdatedAt()
						))
						.toList()
		);
	}

	private List<DashboardStatusCountResponse> buildStatusDistribution() {
		return List.of(
				new DashboardStatusCountResponse(DeliveryStatus.PENDING, deliveryRepository.countByStatus(DeliveryStatus.PENDING)),
				new DashboardStatusCountResponse(DeliveryStatus.ASSIGNED, deliveryRepository.countByStatus(DeliveryStatus.ASSIGNED)),
				new DashboardStatusCountResponse(DeliveryStatus.PICKED_UP, deliveryRepository.countByStatus(DeliveryStatus.PICKED_UP)),
				new DashboardStatusCountResponse(DeliveryStatus.IN_TRANSIT, deliveryRepository.countByStatus(DeliveryStatus.IN_TRANSIT)),
				new DashboardStatusCountResponse(DeliveryStatus.DELIVERED, deliveryRepository.countByStatus(DeliveryStatus.DELIVERED)),
				new DashboardStatusCountResponse(DeliveryStatus.CANCELLED, deliveryRepository.countByStatus(DeliveryStatus.CANCELLED))
		);
	}

	private List<DashboardDailyDeliveryResponse> buildDeliveredLastSevenDays(List<Delivery> deliveries, ZoneId zone) {
		LocalDate today = LocalDate.now(zone);

		return today.minusDays(6)
				.datesUntil(today.plusDays(1))
				.map(date -> new DashboardDailyDeliveryResponse(
						date,
						deliveries.stream()
								.filter(delivery -> delivery.getStatus() == DeliveryStatus.DELIVERED)
								.filter(delivery -> delivery.getUpdatedAt().atZoneSameInstant(zone).toLocalDate().equals(date))
								.count()
				))
				.toList();
	}
}
