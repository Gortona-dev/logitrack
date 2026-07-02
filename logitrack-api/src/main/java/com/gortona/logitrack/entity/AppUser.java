package com.gortona.logitrack.entity;

import com.gortona.logitrack.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Role role;

	@Column(nullable = false)
	private boolean active = true;

	@Column(length = 20)
	private String document;

	@Column(length = 20)
	private String phone;

	private UUID clientId;

	private UUID deliveryPersonId;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	public static AppUser create(String name, String email, String password, Role role) {
		AppUser user = new AppUser();
		user.name = name;
		user.email = email.trim().toLowerCase();
		user.password = password;
		user.role = role;
		user.active = true;
		return user;
	}

	public void updateProfile(String name, String document, String phone) {
		this.name = name;
		this.document = document;
		this.phone = phone;
	}

	public void linkClient(UUID clientId) {
		this.clientId = clientId;
		this.deliveryPersonId = null;
	}

	public void linkDeliveryPerson(UUID deliveryPersonId) {
		this.deliveryPersonId = deliveryPersonId;
		this.clientId = null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
