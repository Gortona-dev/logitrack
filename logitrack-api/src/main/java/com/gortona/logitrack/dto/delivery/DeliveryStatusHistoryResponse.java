package com.gortona.logitrack.dto.delivery;

import com.gortona.logitrack.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryStatusHistoryResponse(
		UUID id,
		UUID deliveryId,
		DeliveryStatus previousStatus,
		DeliveryStatus newStatus,
		String notes,
		OffsetDateTime changedAt
) {
}
