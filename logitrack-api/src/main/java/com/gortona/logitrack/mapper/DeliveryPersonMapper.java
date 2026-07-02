package com.gortona.logitrack.mapper;

import com.gortona.logitrack.dto.deliveryperson.CreateDeliveryPersonRequest;
import com.gortona.logitrack.dto.deliveryperson.DeliveryPersonResponse;
import com.gortona.logitrack.entity.DeliveryPerson;
import org.springframework.stereotype.Component;

@Component
public class DeliveryPersonMapper {

	public DeliveryPerson toEntity(CreateDeliveryPersonRequest request) {
		return DeliveryPerson.create(
				null,
				request.name().trim(),
				request.email().trim().toLowerCase(),
				request.document().trim(),
				request.phone().trim()
		);
	}

	public DeliveryPersonResponse toResponse(DeliveryPerson deliveryPerson) {
		return toResponse(deliveryPerson, null);
	}

	public DeliveryPersonResponse toResponse(DeliveryPerson deliveryPerson, String assignedVehicle) {
		return new DeliveryPersonResponse(
				deliveryPerson.getId(),
				deliveryPerson.getCode(),
				deliveryPerson.getName(),
				deliveryPerson.getEmail(),
				deliveryPerson.getDocument(),
				deliveryPerson.getPhone(),
				deliveryPerson.getStatus(),
				deliveryPerson.isActive(),
				assignedVehicle,
				deliveryPerson.getCreatedAt(),
				deliveryPerson.getUpdatedAt()
		);
	}
}
