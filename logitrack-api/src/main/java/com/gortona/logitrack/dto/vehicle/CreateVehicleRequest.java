package com.gortona.logitrack.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(
		@NotBlank(message = "Placa é obrigatória")
		@Size(max = 12, message = "Placa deve ter no máximo 12 caracteres")
		@Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Placa contém caracteres inválidos")
		String licensePlate,

		@NotBlank(message = "Marca é obrigatória")
		@Size(max = 80, message = "Marca deve ter no máximo 80 caracteres")
		String brand,

		@NotBlank(message = "Modelo é obrigatório")
		@Size(max = 80, message = "Modelo deve ter no máximo 80 caracteres")
		String model
) {
}
