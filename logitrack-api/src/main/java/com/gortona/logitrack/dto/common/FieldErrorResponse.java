package com.gortona.logitrack.dto.common;

public record FieldErrorResponse(
		String field,
		String message
) {
}
