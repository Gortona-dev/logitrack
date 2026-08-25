package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

	private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

	@GetMapping
	public ResponseEntity<ApiResponse<String>> check() {
		return ResponseEntity.ok(ApiResponse.success("Aplicacao disponivel", "OK"));
	}

	@GetMapping("/db")
	public ResponseEntity<ApiResponse<String>> checkDatabase() {
		JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
		if (jdbcTemplate == null) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(ApiResponse.success("Banco indisponivel", "UNAVAILABLE"));
		}

		Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);
		String status = Integer.valueOf(1).equals(result) ? "OK" : "UNAVAILABLE";

		return ResponseEntity.ok(ApiResponse.success("Banco disponivel", status));
	}
}
