package com.gortona.logitrack.dto.user;

import com.gortona.logitrack.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
		String name,

		@NotBlank(message = "Email é obrigatório")
		@Email(message = "Email deve ser válido")
		@Size(max = 160, message = "Email deve ter no máximo 160 caracteres")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
		String password,

		@NotNull(message = "Perfil é obrigatório")
		Role role,

		@Size(max = 20, message = "CPF/documento deve ter no máximo 20 caracteres")
		String document,

		@Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
		String phone
) {
}
