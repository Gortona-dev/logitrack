package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.dto.vehicle.CreateVehicleRequest;
import com.gortona.logitrack.dto.vehicle.UpdateVehicleRequest;
import com.gortona.logitrack.dto.vehicle.VehicleResponse;
import com.gortona.logitrack.service.VehicleService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

	private final VehicleService vehicleService;

	@PostMapping
	public ResponseEntity<ApiResponse<VehicleResponse>> create(@Valid @RequestBody CreateVehicleRequest request) {
		VehicleResponse response = vehicleService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Veículo criado com sucesso", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<VehicleResponse>>> findAll() {
		List<VehicleResponse> response = vehicleService.findAll();
		return ResponseEntity.ok(ApiResponse.success("Veículos consultados com sucesso", response));
	}
	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<VehicleResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateVehicleRequest request
	) {
		VehicleResponse response = vehicleService.update(id, request);
		return ResponseEntity.ok(ApiResponse.success("Veículo atualizado com sucesso", response));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
		vehicleService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Veículo removido com sucesso", null));
	}
}
