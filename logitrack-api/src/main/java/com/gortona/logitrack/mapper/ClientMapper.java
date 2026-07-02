package com.gortona.logitrack.mapper;

import com.gortona.logitrack.dto.client.ClientResponse;
import com.gortona.logitrack.dto.client.CreateClientRequest;
import com.gortona.logitrack.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

	public Client toEntity(CreateClientRequest request) {
		return Client.create(
				null,
				request.name().trim(),
				request.email().trim().toLowerCase(),
				request.document().trim(),
				request.phone().trim()
		);
	}

	public ClientResponse toResponse(Client client) {
		return new ClientResponse(
				client.getId(),
				client.getCode(),
				client.getName(),
				client.getEmail(),
				client.getDocument(),
				client.getPhone(),
				client.getCreatedAt(),
				client.getUpdatedAt(),
				client.getDeletedAt()
		);
	}
}
