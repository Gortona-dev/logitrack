package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.auth.LoginRequest;
import com.gortona.logitrack.dto.auth.LoginResponse;
import com.gortona.logitrack.dto.auth.MeResponse;
import com.gortona.logitrack.dto.auth.UpdateMeRequest;
import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success("Login realizado com sucesso", response));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<MeResponse>> me(@AuthenticationPrincipal AppUser user) {
		MeResponse response = authService.me(user);
		return ResponseEntity.ok(ApiResponse.success("Usuário autenticado consultado com sucesso", response));
	}
	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<MeResponse>> updateMe(
			@AuthenticationPrincipal AppUser user,
			@Valid @RequestBody UpdateMeRequest request
	) {
		MeResponse response = authService.updateMe(user, request);
		return ResponseEntity.ok(ApiResponse.success("Perfil atualizado com sucesso", response));
	}
}
