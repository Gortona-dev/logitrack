package com.gortona.logitrack.dto.auth;

import com.gortona.logitrack.enums.Role;

public record LoginResponse(
		String token,
		Role role
) {
}
