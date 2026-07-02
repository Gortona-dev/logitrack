package com.gortona.logitrack.dto.dashboard;

import com.gortona.logitrack.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DashboardDeliveryResponse(
		UUID orderId,
		String clientName,
		String deliveryAddress,
		DeliveryStatus status,
		OffsetDateTime updatedAt
) {
}
