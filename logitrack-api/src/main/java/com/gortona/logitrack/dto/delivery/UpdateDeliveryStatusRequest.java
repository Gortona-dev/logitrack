package com.gortona.logitrack.dto.delivery;

import com.gortona.logitrack.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDeliveryStatusRequest(
		@NotNull(message = "Status é obrigatório")
		DeliveryStatus status,

		@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
		String notes
) {
}
