package com.gortona.logitrack.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
		String name,

		@Size(max = 20, message = "CPF deve ter no máximo 20 caracteres")
		String document,

		@Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
		String phone
) {
}
