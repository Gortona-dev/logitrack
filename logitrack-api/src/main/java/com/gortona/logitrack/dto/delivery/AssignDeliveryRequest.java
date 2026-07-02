package com.gortona.logitrack.dto.delivery;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDeliveryRequest(
		@NotNull(message = "Id do entregador é obrigatório")
		UUID deliveryPersonId,

		@NotNull(message = "Id do veículo é obrigatório")
		UUID vehicleId
) {
}
