package com.gortona.logitrack.dto.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
		OffsetDateTime timestamp,
		boolean success,
		int status,
		String error,
		String message,
		String path,
		List<FieldErrorResponse> fieldErrors
) {

	public static ApiErrorResponse of(int status, String error, String message, String path) {
		return new ApiErrorResponse(OffsetDateTime.now(), false, status, error, message, path, List.of());
	}

	public static ApiErrorResponse of(
			int status,
			String error,
			String message,
			String path,
			List<FieldErrorResponse> fieldErrors
	) {
		return new ApiErrorResponse(OffsetDateTime.now(), false, status, error, message, path, fieldErrors);
	}
}
