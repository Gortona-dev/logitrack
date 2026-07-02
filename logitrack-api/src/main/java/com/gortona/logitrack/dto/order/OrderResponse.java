package com.gortona.logitrack.dto.order;

import com.gortona.logitrack.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderResponse(
		UUID id,
		String trackingCode,
		UUID clientId,
		String clientCode,
		String clientName,
		String pickupAddress,
		String deliveryAddress,
		String description,
		UUID deliveryId,
		DeliveryStatus status,
		UUID deliveryPersonId,
		String deliveryPersonCode,
		UUID vehicleId,
		String vehicleCode,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
