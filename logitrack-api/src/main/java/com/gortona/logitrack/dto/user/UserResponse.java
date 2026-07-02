package com.gortona.logitrack.dto.user;

import com.gortona.logitrack.enums.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String name,
		String email,
		Role role,
		boolean active,
		String document,
		String phone,
		UUID clientId,
		UUID deliveryPersonId,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
