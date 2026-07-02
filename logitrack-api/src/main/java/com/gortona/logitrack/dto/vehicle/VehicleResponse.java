package com.gortona.logitrack.dto.vehicle;

import com.gortona.logitrack.enums.VehicleStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleResponse(
		UUID id,
		String code,
		String licensePlate,
		String brand,
		String model,
		boolean active,
		VehicleStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
