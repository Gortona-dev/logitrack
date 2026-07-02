package com.gortona.logitrack.dto.client;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
		UUID id,
		String code,
		String name,
		String email,
		String document,
		String phone,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime deletedAt
) {
}
