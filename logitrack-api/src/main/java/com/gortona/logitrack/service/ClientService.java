package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.client.ClientResponse;
import com.gortona.logitrack.dto.client.CreateClientRequest;
import com.gortona.logitrack.dto.client.UpdateClientRequest;
import com.gortona.logitrack.dto.common.PageResponse;
import com.gortona.logitrack.entity.Client;
import com.gortona.logitrack.exception.ConflictException;
import com.gortona.logitrack.exception.ResourceNotFoundException;
import com.gortona.logitrack.mapper.ClientMapper;
import com.gortona.logitrack.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

	private final ClientRepository clientRepository;
	private final ClientMapper clientMapper;
	private final FriendlyCodeService friendlyCodeService;

	@Transactional
	public ClientResponse create(CreateClientRequest request) {
		validateUniqueClient(request);

		Client client = clientMapper.toEntity(request);
		client.setCode(friendlyCodeService.nextClientCode());
		Client savedClient = clientRepository.save(client);

		return clientMapper.toResponse(savedClient);
	}

	@Transactional(readOnly = true)
	public List<ClientResponse> findAll() {
		return clientRepository.findByDeletedAtIsNull(PageRequest.of(0, 200, Sort.by("name").ascending()))
				.getContent()
				.stream()
				.map(clientMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public PageResponse<ClientResponse> findPage(String search, int page, int size) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.clamp(size, 1, 50);
		String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
		PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());

		if (normalizedSearch == null) {
			return PageResponse.from(clientRepository.findByDeletedAtIsNull(pageRequest).map(clientMapper::toResponse));
		}

		return PageResponse.from(clientRepository.searchActive(normalizedSearch, pageRequest).map(clientMapper::toResponse));
	}

	@Transactional(readOnly = true)
	public ClientResponse findById(UUID id) {
		return clientMapper.toResponse(findActiveClient(id));
	}

	@Transactional
	public ClientResponse update(UUID id, UpdateClientRequest request) {
		Client client = findActiveClient(id);
		validateUniqueClientForUpdate(id, request);

		client.update(
				request.name().trim(),
				request.email().trim().toLowerCase(),
				request.document().trim(),
				request.phone().trim()
		);

		return clientMapper.toResponse(clientRepository.save(client));
	}

	@Transactional
	public void delete(UUID id) {
		Client client = findActiveClient(id);
		client.softDelete();
		clientRepository.save(client);
	}

	private Client findActiveClient(UUID id) {
		return clientRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
	}

	private void validateUniqueClient(CreateClientRequest request) {
		String email = request.email().trim().toLowerCase();
		String document = request.document().trim();

		if (clientRepository.existsByEmail(email)) {
			throw new ConflictException("Já existe um cliente com este email");
		}

		if (clientRepository.existsByDocument(document)) {
			throw new ConflictException("Já existe um cliente com este documento");
		}
	}

	private void validateUniqueClientForUpdate(UUID id, UpdateClientRequest request) {
		String email = request.email().trim().toLowerCase();
		String document = request.document().trim();

		if (clientRepository.existsByEmailAndIdNot(email, id)) {
			throw new ConflictException("Já existe um cliente com este email");
		}

		if (clientRepository.existsByDocumentAndIdNot(document, id)) {
			throw new ConflictException("Já existe um cliente com este documento");
		}
	}
}
