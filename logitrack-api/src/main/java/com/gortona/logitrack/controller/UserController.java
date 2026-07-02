package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.dto.user.CreateUserRequest;
import com.gortona.logitrack.dto.user.UpdateUserRequest;
import com.gortona.logitrack.dto.user.UserResponse;
import com.gortona.logitrack.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
		List<UserResponse> response = userService.findAll();
		return ResponseEntity.ok(ApiResponse.success("Usuários consultados com sucesso", response));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
		UserResponse response = userService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Usuário criado com sucesso", response));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request
	) {
		UserResponse response = userService.update(id, request);
		return ResponseEntity.ok(ApiResponse.success("Usuário atualizado com sucesso", response));
	}
}
