package com.gortona.logitrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "clients")
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@Column(nullable = false, unique = true, length = 20)
	private String document;

	@Column(nullable = false, length = 20)
	private String phone;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	private OffsetDateTime deletedAt;

	public static Client create(String code, String name, String email, String document, String phone) {
		Client client = new Client();
		client.code = code;
		client.name = name;
		client.email = email;
		client.document = document;
		client.phone = phone;
		return client;
	}

	public void update(String name, String email, String document, String phone) {
		this.name = name;
		this.email = email;
		this.document = document;
		this.phone = phone;
	}

	public void softDelete() {
		deletedAt = OffsetDateTime.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

}
