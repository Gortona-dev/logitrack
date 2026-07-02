package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.auth.LoginRequest;
import com.gortona.logitrack.dto.auth.LoginResponse;
import com.gortona.logitrack.dto.auth.MeResponse;
import com.gortona.logitrack.dto.auth.UpdateMeRequest;
import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.entity.Client;
import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.enums.Role;
import com.gortona.logitrack.exception.ConflictException;
import com.gortona.logitrack.exception.UnauthorizedException;
import com.gortona.logitrack.repository.AppUserRepository;
import com.gortona.logitrack.repository.ClientRepository;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import com.gortona.logitrack.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final ClientRepository clientRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		AppUser user = appUserRepository.findByEmail(request.email().trim().toLowerCase())
				.filter(candidate -> candidate.isEnabled())
				.filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
				.orElseThrow(() -> new UnauthorizedException("Email ou senha inválidos"));

		return new LoginResponse(jwtService.generateToken(user), user.getRole());
	}

	@Transactional(readOnly = true)
	public MeResponse me(AppUser user) {
		return toMeResponse(user);
	}

	@Transactional
	public MeResponse updateMe(AppUser user, UpdateMeRequest request) {
		String name = request.name().trim();
		String document = normalizeOptional(request.document());
		String phone = normalizeOptional(request.phone());

		user.updateProfile(name, document, phone);
		syncLinkedProfile(user, name, document, phone);

		return toMeResponse(appUserRepository.save(user));
	}

	private MeResponse toMeResponse(AppUser user) {
		return new MeResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getDocument(),
				user.getPhone(),
				user.getClientId(),
				user.getDeliveryPersonId()
		);
	}

	private void syncLinkedProfile(AppUser user, String name, String document, String phone) {
		if (user.getRole() == Role.CLIENTE && user.getClientId() != null) {
			Client client = clientRepository.findByIdAndDeletedAtIsNull(user.getClientId()).orElse(null);
			if (client != null) {
				if (document != null && clientRepository.existsByDocumentAndIdNot(document, client.getId())) {
					throw new ConflictException("Já existe um cliente com este CPF/documento");
				}
				client.update(
						name,
						client.getEmail(),
						document == null ? client.getDocument() : document,
						phone == null ? client.getPhone() : phone
				);
			}
		}

		if (user.getRole() == Role.ENTREGADOR && user.getDeliveryPersonId() != null) {
			DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(user.getDeliveryPersonId()).orElse(null);
			if (deliveryPerson != null && deliveryPerson.isActive()) {
				if (document != null && deliveryPersonRepository.existsByDocumentAndIdNot(document, deliveryPerson.getId())) {
					throw new ConflictException("Já existe um entregador com este CPF/documento");
				}
				deliveryPerson.setName(name);
				if (document != null) {
					deliveryPerson.setDocument(document);
				}
				if (phone != null) {
					deliveryPerson.setPhone(phone);
				}
			}
		}
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
