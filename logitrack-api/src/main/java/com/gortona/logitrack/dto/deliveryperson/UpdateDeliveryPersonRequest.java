package com.gortona.logitrack.dto.deliveryperson;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateDeliveryPersonRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
		String name,

		@NotBlank(message = "Email é obrigatório")
		@Email(message = "Email deve ser válido")
		@Size(max = 160, message = "Email deve ter no máximo 160 caracteres")
		String email,

		@NotBlank(message = "Documento é obrigatório")
		@Size(max = 20, message = "Documento deve ter no máximo 20 caracteres")
		@Pattern(regexp = "^[0-9]+$", message = "Documento deve conter apenas números")
		String document,

		@NotBlank(message = "Telefone é obrigatório")
		@Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
		@Pattern(regexp = "^[0-9+()\\-\\s]+$", message = "Telefone contém caracteres inválidos")
		String phone
) {
}
