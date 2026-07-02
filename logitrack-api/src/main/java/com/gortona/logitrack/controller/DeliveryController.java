package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.dto.delivery.AssignDeliveryRequest;
import com.gortona.logitrack.dto.delivery.DeliveryResponse;
import com.gortona.logitrack.dto.delivery.DeliveryStatusHistoryResponse;
import com.gortona.logitrack.dto.delivery.UpdateDeliveryStatusRequest;
import com.gortona.logitrack.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryService deliveryService;

	@PostMapping("/{orderId}/assign")
	public ResponseEntity<ApiResponse<DeliveryResponse>> assign(
			@PathVariable UUID orderId,
			@Valid @RequestBody AssignDeliveryRequest request
	) {
		DeliveryResponse response = deliveryService.assign(orderId, request);
		return ResponseEntity.ok(ApiResponse.success("Entrega atribuída com sucesso", response));
	}

	@PatchMapping("/{deliveryId}/status")
	public ResponseEntity<ApiResponse<DeliveryResponse>> updateStatus(
			@PathVariable UUID deliveryId,
			@Valid @RequestBody UpdateDeliveryStatusRequest request
	) {
		DeliveryResponse response = deliveryService.updateStatus(deliveryId, request);
		return ResponseEntity.ok(ApiResponse.success("Status da entrega atualizado com sucesso", response));
	}

	@GetMapping("/{deliveryId}/history")
	public ResponseEntity<ApiResponse<List<DeliveryStatusHistoryResponse>>> findHistory(@PathVariable UUID deliveryId) {
		List<DeliveryStatusHistoryResponse> response = deliveryService.findHistory(deliveryId);
		return ResponseEntity.ok(ApiResponse.success("Histórico da entrega consultado com sucesso", response));
	}
}
