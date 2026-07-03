package com.gortona.logitrack.config;

import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.entity.Client;
import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.enums.Role;
import com.gortona.logitrack.repository.AppUserRepository;
import com.gortona.logitrack.repository.ClientRepository;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DevelopmentUserSeed {

	private final AppUserRepository appUserRepository;
	private final ClientRepository clientRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final PasswordEncoder passwordEncoder;

	@Bean
	public CommandLineRunner seedDefaultUsers() {
		return args -> {
			Client client = findSeedClient()
					.orElseGet(() -> saveClient(Client.create(
							"CLI-0001",
							"Cliente Teste",
							"cliente@logitrack.com",
							"10000000001",
							"11999990001"
					)));

			DeliveryPerson deliveryPerson = findSeedDeliveryPerson()
					.orElseGet(() -> saveDeliveryPerson(DeliveryPerson.create(
							"ENT-0001",
							"Entregador Teste",
							"entregador@logitrack.com",
							"20000000002",
							"11999990002"
					)));

			upsertUser("Administrador", "admin@logitrack.com", "admin123", Role.ADMIN, null, null, "00000000000", "11999990000");
			upsertUser("Operador", "operador@logitrack.com", "operador123", Role.OPERADOR, null, null, "00000000010", "11999990010");
			upsertUser("Entregador Teste", "entregador@logitrack.com", "entregador123", Role.ENTREGADOR, null, deliveryPerson, deliveryPerson.getDocument(), deliveryPerson.getPhone());
			upsertUser("Cliente Teste", "cliente@logitrack.com", "cliente123", Role.CLIENTE, client, null, client.getDocument(), client.getPhone());
			resetExistingUsersPasswords();
		};
	}

	private Optional<Client> findSeedClient() {
		Optional<Client> clientByEmail = Optional.ofNullable(clientRepository.findByEmail("cliente@logitrack.com"))
				.orElse(Optional.empty());

		return clientByEmail.isPresent()
				? clientByEmail
				: Optional.ofNullable(clientRepository.findByCode("CLI-0001")).orElse(Optional.empty());
	}

	private Optional<DeliveryPerson> findSeedDeliveryPerson() {
		Optional<DeliveryPerson> deliveryPersonByEmail = Optional.ofNullable(deliveryPersonRepository.findByEmail("entregador@logitrack.com"))
				.orElse(Optional.empty());

		return deliveryPersonByEmail.isPresent()
				? deliveryPersonByEmail
				: Optional.ofNullable(deliveryPersonRepository.findByCode("ENT-0001")).orElse(Optional.empty());
	}

	private void upsertUser(
			String name,
			String email,
			String rawPassword,
			Role role,
			Client client,
			DeliveryPerson deliveryPerson,
			String document,
			String phone
	) {
		AppUser user = Optional.ofNullable(appUserRepository.findByEmail(email)).orElse(Optional.empty())
				.orElseGet(() -> AppUser.create(name, email, passwordEncoder.encode(rawPassword), role));

		user.setName(name);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(role);
		user.setActive(true);
		user.updateProfile(name, document, phone);

		if (client != null) {
			user.linkClient(client.getId());
		}

		if (deliveryPerson != null) {
			user.linkDeliveryPerson(deliveryPerson.getId());
		}

		appUserRepository.save(user);
	}

	private void resetExistingUsersPasswords() {
		List<AppUser> users = Optional.ofNullable(appUserRepository.findAll()).orElse(List.of());

		users.stream()
				.filter(user -> !isDefaultTestUser(user))
				.forEach(user -> {
					user.setPassword(passwordEncoder.encode(defaultPasswordFor(user)));
					appUserRepository.save(user);
				});
	}

	private boolean isDefaultTestUser(AppUser user) {
		return List.of(
				"admin@logitrack.com",
				"operador@logitrack.com",
				"entregador@logitrack.com",
				"cliente@logitrack.com"
		).contains(user.getEmail());
	}

	private String defaultPasswordFor(AppUser user) {
		String source = Optional.ofNullable(user.getName())
				.filter(name -> !name.isBlank())
				.orElseGet(() -> user.getEmail().split("@")[0]);
		String firstName = source.trim().split("\\s+")[0];
		String normalizedName = Normalizer.normalize(firstName, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replaceAll("[^A-Za-z0-9]", "")
				.toLowerCase(Locale.ROOT);

		return normalizedName.isBlank() ? "usuario123" : normalizedName + "123";
	}

	private Client saveClient(Client client) {
		Client savedClient = clientRepository.save(client);
		return savedClient == null ? client : savedClient;
	}

	private DeliveryPerson saveDeliveryPerson(DeliveryPerson deliveryPerson) {
		DeliveryPerson savedDeliveryPerson = deliveryPersonRepository.save(deliveryPerson);
		return savedDeliveryPerson == null ? deliveryPerson : savedDeliveryPerson;
	}
}
