package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

	@GetMapping
	public ResponseEntity<ApiResponse<String>> check() {
		return ResponseEntity.ok(ApiResponse.success("Aplicacao disponivel", "OK"));
	}
}
