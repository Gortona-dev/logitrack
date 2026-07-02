package com.gortona.logitrack.mapper;

import com.gortona.logitrack.dto.delivery.DeliveryResponse;
import com.gortona.logitrack.dto.delivery.DeliveryStatusHistoryResponse;
import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.entity.DeliveryStatusHistory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DeliveryMapper {

	public DeliveryResponse toResponse(Delivery delivery) {
		return new DeliveryResponse(
				delivery.getId(),
				delivery.getOrder().getId(),
				Objects.nonNull(delivery.getDeliveryPerson()) ? delivery.getDeliveryPerson().getId() : null,
				Objects.nonNull(delivery.getVehicle()) ? delivery.getVehicle().getId() : null,
				delivery.getStatus(),
				delivery.getCreatedAt(),
				delivery.getUpdatedAt()
		);
	}

	public DeliveryStatusHistoryResponse toHistoryResponse(DeliveryStatusHistory history) {
		return new DeliveryStatusHistoryResponse(
				history.getId(),
				history.getDelivery().getId(),
				history.getPreviousStatus(),
				history.getNewStatus(),
				history.getNotes(),
				history.getChangedAt()
		);
	}
}
