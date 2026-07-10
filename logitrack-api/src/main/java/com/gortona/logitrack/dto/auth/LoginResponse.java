package com.gortona.logitrack.dto.auth;

import com.gortona.logitrack.enums.Role;

import java.util.UUID;

public record LoginResponse(
		String token,
		Role role,
		String name,
		String email,
		String document,
		String phone,
		UUID clientId,
		UUID deliveryPersonId
) {
}
