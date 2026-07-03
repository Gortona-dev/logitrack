package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.dto.common.PageResponse;
import com.gortona.logitrack.dto.deliveryperson.CreateDeliveryPersonRequest;
import com.gortona.logitrack.dto.deliveryperson.DeliveryPersonResponse;
import com.gortona.logitrack.dto.deliveryperson.UpdateDeliveryPersonRequest;
import com.gortona.logitrack.service.DeliveryPersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-persons")
@RequiredArgsConstructor
public class DeliveryPersonController {

	private final DeliveryPersonService deliveryPersonService;

	@PostMapping
	public ResponseEntity<ApiResponse<DeliveryPersonResponse>> create(
			@Valid @RequestBody CreateDeliveryPersonRequest request
	) {
		DeliveryPersonResponse response = deliveryPersonService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Entregador criado com sucesso", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<DeliveryPersonResponse>>> findAll() {
		List<DeliveryPersonResponse> response = deliveryPersonService.findAll();
		return ResponseEntity.ok(ApiResponse.success("Entregadores consultados com sucesso", response));
	}

	@GetMapping("/page")
	public ResponseEntity<ApiResponse<PageResponse<DeliveryPersonResponse>>> findPage(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		PageResponse<DeliveryPersonResponse> response = deliveryPersonService.findPage(search, page, size);
		return ResponseEntity.ok(ApiResponse.success("Entregadores consultados com sucesso", response));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<DeliveryPersonResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateDeliveryPersonRequest request
	) {
		DeliveryPersonResponse response = deliveryPersonService.update(id, request);
		return ResponseEntity.ok(ApiResponse.success("Entregador atualizado com sucesso", response));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
		deliveryPersonService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Entregador removido com sucesso", null));
	}
}
