package com.gortona.logitrack.dto.delivery;

import com.gortona.logitrack.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryResponse(
		UUID id,
		UUID orderId,
		UUID deliveryPersonId,
		UUID vehicleId,
		DeliveryStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
