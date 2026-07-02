package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.client.ClientResponse;
import com.gortona.logitrack.dto.client.CreateClientRequest;
import com.gortona.logitrack.dto.client.UpdateClientRequest;
import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.dto.common.PageResponse;
import com.gortona.logitrack.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

	private final ClientService clientService;

	@PostMapping
	public ResponseEntity<ApiResponse<ClientResponse>> create(@Valid @RequestBody CreateClientRequest request) {
		ClientResponse response = clientService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Cliente criado com sucesso", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ClientResponse>>> findAll() {
		List<ClientResponse> response = clientService.findAll();
		return ResponseEntity.ok(ApiResponse.success("Clientes consultados com sucesso", response));
	}

	@GetMapping("/page")
	public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> findPage(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		PageResponse<ClientResponse> response = clientService.findPage(search, page, size);
		return ResponseEntity.ok(ApiResponse.success("Clientes consultados com sucesso", response));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientResponse>> findById(@PathVariable UUID id) {
		ClientResponse response = clientService.findById(id);
		return ResponseEntity.ok(ApiResponse.success("Cliente consultado com sucesso", response));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateClientRequest request
	) {
		ClientResponse response = clientService.update(id, request);
		return ResponseEntity.ok(ApiResponse.success("Cliente atualizado com sucesso", response));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
		clientService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Cliente removido com sucesso", null));
	}
}
