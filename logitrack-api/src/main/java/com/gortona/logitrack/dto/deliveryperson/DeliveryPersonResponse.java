package com.gortona.logitrack.dto.deliveryperson;

import com.gortona.logitrack.enums.DeliveryPersonStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryPersonResponse(
		UUID id,
		String code,
		String name,
		String email,
		String document,
		String phone,
		DeliveryPersonStatus status,
		boolean active,
		String assignedVehicle,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
