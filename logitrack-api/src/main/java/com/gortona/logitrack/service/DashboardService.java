package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.dashboard.DashboardDeliveryResponse;
import com.gortona.logitrack.dto.dashboard.DashboardResponse;
import com.gortona.logitrack.enums.DeliveryPersonStatus;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.enums.VehicleStatus;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final DeliveryRepository deliveryRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final VehicleRepository vehicleRepository;

	@Transactional(readOnly = true)
	public DashboardResponse getOverview() {
		ZoneId zone = ZoneId.of("America/Sao_Paulo");
		OffsetDateTime startOfDay = LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();
		OffsetDateTime endOfDay = startOfDay.plusDays(1);

		return new DashboardResponse(
				deliveryRepository.countByStatus(DeliveryStatus.PENDING),
				deliveryRepository.countByStatus(DeliveryStatus.ASSIGNED),
				deliveryRepository.countByStatus(DeliveryStatus.IN_TRANSIT),
				deliveryRepository.countByStatusAndUpdatedAtBetween(DeliveryStatus.DELIVERED, startOfDay, endOfDay),
				deliveryRepository.countByStatus(DeliveryStatus.CANCELLED),
				deliveryPersonRepository.countByStatus(DeliveryPersonStatus.AVAILABLE),
				deliveryPersonRepository.countByStatus(DeliveryPersonStatus.ON_DELIVERY),
				vehicleRepository.countByActiveTrue(),
				vehicleRepository.countByStatusAndActiveTrue(VehicleStatus.MAINTENANCE),
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
}
