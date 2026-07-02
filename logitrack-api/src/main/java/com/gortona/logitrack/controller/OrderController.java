package com.gortona.logitrack.controller;

import com.gortona.logitrack.dto.common.ApiResponse;
import com.gortona.logitrack.dto.order.CreateOrderRequest;
import com.gortona.logitrack.dto.order.OrderResponse;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
		OrderResponse response = orderService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Pedido criado com sucesso", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<OrderResponse>>> findAll(
			@RequestParam(required = false) UUID clientId,
			@RequestParam(required = false) UUID deliveryPersonId,
			@RequestParam(required = false) DeliveryStatus status
	) {
		List<OrderResponse> response = orderService.findAll(clientId, deliveryPersonId, status);
		return ResponseEntity.ok(ApiResponse.success("Pedidos consultados com sucesso", response));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable UUID id) {
		OrderResponse response = orderService.findById(id);
		return ResponseEntity.ok(ApiResponse.success("Pedido consultado com sucesso", response));
	}
}
