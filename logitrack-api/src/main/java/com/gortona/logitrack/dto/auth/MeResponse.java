package com.gortona.logitrack.dto.auth;

import com.gortona.logitrack.enums.Role;

import java.util.UUID;

public record MeResponse(
		UUID id,
		String name,
		String email,
		Role role,
		String document,
		String phone,
		UUID clientId,
		UUID deliveryPersonId
) {
}
