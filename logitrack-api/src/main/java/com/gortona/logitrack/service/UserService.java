package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.user.CreateUserRequest;
import com.gortona.logitrack.dto.user.UpdateUserRequest;
import com.gortona.logitrack.dto.user.UserResponse;
import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.entity.Client;
import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.enums.Role;
import com.gortona.logitrack.exception.BusinessRuleException;
import com.gortona.logitrack.exception.ConflictException;
import com.gortona.logitrack.mapper.UserMapper;
import com.gortona.logitrack.repository.AppUserRepository;
import com.gortona.logitrack.repository.ClientRepository;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private final AppUserRepository appUserRepository;
	private final ClientRepository clientRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final FriendlyCodeService friendlyCodeService;

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return appUserRepository.findAll(Sort.by("createdAt").descending())
				.stream()
				.map(userMapper::toResponse)
				.toList();
	}

	@Transactional
	public UserResponse create(CreateUserRequest request) {
		String name = request.name().trim();
		String email = request.email().trim().toLowerCase();
		String document = normalizeRequiredForOperationalRole(request);
		String phone = normalizePhone(request);

		if (appUserRepository.existsByEmail(email)) {
			throw new ConflictException("Já existe um usuário com este email");
		}

		AppUser user = AppUser.create(name, email, passwordEncoder.encode(request.password()), request.role());
		user.updateProfile(name, normalizeOptional(request.document()), normalizeOptional(request.phone()));

		if (request.role() == Role.CLIENTE) {
			Client client = createClient(name, email, document, phone);
			user.linkClient(client.getId());
			user.updateProfile(name, document, phone);
		}

		if (request.role() == Role.ENTREGADOR) {
			DeliveryPerson deliveryPerson = createDeliveryPerson(name, email, document, phone);
			user.linkDeliveryPerson(deliveryPerson.getId());
			user.updateProfile(name, document, phone);
		}

		return userMapper.toResponse(appUserRepository.save(user));
	}

	@Transactional
	public UserResponse update(UUID id, UpdateUserRequest request) {
		AppUser user = appUserRepository.findById(id)
				.orElseThrow(() -> new BusinessRuleException("Usuário não encontrado"));
		String name = request.name().trim();
		String email = request.email().trim().toLowerCase();
		String document = normalizeOptional(request.document());
		String phone = normalizeOptional(request.phone());

		if (appUserRepository.existsByEmailAndIdNot(email, id)) {
			throw new ConflictException("Já existe um usuário com este email");
		}

		user.setName(name);
		user.setEmail(email);
		user.updateProfile(name, document, phone);

		if (request.password() != null && !request.password().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}

		syncLinkedProfile(user, name, email, document, phone);

		return userMapper.toResponse(appUserRepository.save(user));
	}

	private Client createClient(String name, String email, String document, String phone) {
		if (clientRepository.existsByEmail(email)) {
			throw new ConflictException("Já existe um cliente com este email");
		}

		if (clientRepository.existsByDocument(document)) {
			throw new ConflictException("Já existe um cliente com este CPF/documento");
		}

		return clientRepository.save(Client.create(friendlyCodeService.nextClientCode(), name, email, document, phone));
	}

	private DeliveryPerson createDeliveryPerson(String name, String email, String document, String phone) {
		if (deliveryPersonRepository.existsByEmail(email)) {
			throw new ConflictException("Já existe um entregador com este email");
		}

		if (deliveryPersonRepository.existsByDocument(document)) {
			throw new ConflictException("Já existe um entregador com este CPF/documento");
		}

		return deliveryPersonRepository.save(DeliveryPerson.create(friendlyCodeService.nextDeliveryPersonCode(), name, email, document, phone));
	}

	private void syncLinkedProfile(AppUser user, String name, String email, String document, String phone) {
		if (user.getRole() == Role.CLIENTE && user.getClientId() != null) {
			Client client = clientRepository.findById(user.getClientId()).orElse(null);
			if (client != null && !client.isDeleted()) {
				if (clientRepository.existsByEmailAndIdNot(email, client.getId())) {
					throw new ConflictException("Já existe um cliente com este email");
				}
				if (document != null && clientRepository.existsByDocumentAndIdNot(document, client.getId())) {
					throw new ConflictException("Já existe um cliente com este CPF/documento");
				}
				client.update(name, email, document == null ? client.getDocument() : document, phone == null ? client.getPhone() : phone);
			}
		}

		if (user.getRole() == Role.ENTREGADOR && user.getDeliveryPersonId() != null) {
			DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(user.getDeliveryPersonId()).orElse(null);
			if (deliveryPerson != null && deliveryPerson.isActive()) {
				if (deliveryPersonRepository.existsByEmailAndIdNot(email, deliveryPerson.getId())) {
					throw new ConflictException("Já existe um entregador com este email");
				}
				if (document != null && deliveryPersonRepository.existsByDocumentAndIdNot(document, deliveryPerson.getId())) {
					throw new ConflictException("Já existe um entregador com este CPF/documento");
				}
				deliveryPerson.setName(name);
				deliveryPerson.setEmail(email);
				if (document != null) {
					deliveryPerson.setDocument(document);
				}
				if (phone != null) {
					deliveryPerson.setPhone(phone);
				}
			}
		}
	}

	private String normalizeRequiredForOperationalRole(CreateUserRequest request) {
		if (request.role() != Role.CLIENTE && request.role() != Role.ENTREGADOR) {
			return normalizeOptional(request.document());
		}

		String document = normalizeOptional(request.document());
		if (document == null) {
			throw new BusinessRuleException("CPF/documento é obrigatório para cliente e entregador");
		}

		return document;
	}

	private String normalizePhone(CreateUserRequest request) {
		if (request.role() != Role.CLIENTE && request.role() != Role.ENTREGADOR) {
			return normalizeOptional(request.phone());
		}

		String phone = normalizeOptional(request.phone());
		if (phone == null) {
			throw new BusinessRuleException("Telefone é obrigatório para cliente e entregador");
		}

		return phone;
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
