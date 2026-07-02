package com.gortona.logitrack.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrderRequest(
		@NotNull(message = "Id do cliente é obrigatório")
		UUID clientId,

		@NotBlank(message = "Endereço de coleta é obrigatório")
		@Size(max = 255, message = "Endereço de coleta deve ter no máximo 255 caracteres")
		String pickupAddress,

		@NotBlank(message = "Endereço de entrega é obrigatório")
		@Size(max = 255, message = "Endereço de entrega deve ter no máximo 255 caracteres")
		String deliveryAddress,

		@NotBlank(message = "Descrição é obrigatória")
		@Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
		String description
) {
}
