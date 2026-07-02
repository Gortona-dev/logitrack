package com.gortona.logitrack.entity;

import com.gortona.logitrack.enums.DeliveryPersonStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "delivery_persons")
public class DeliveryPerson {

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DeliveryPersonStatus status = DeliveryPersonStatus.AVAILABLE;

	@Column(nullable = false)
	private boolean active = true;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	public static DeliveryPerson create(String code, String name, String email, String document, String phone) {
		DeliveryPerson deliveryPerson = new DeliveryPerson();
		deliveryPerson.code = code;
		deliveryPerson.name = name;
		deliveryPerson.email = email;
		deliveryPerson.document = document;
		deliveryPerson.phone = phone;
		deliveryPerson.status = DeliveryPersonStatus.AVAILABLE;
		deliveryPerson.active = true;
		return deliveryPerson;
	}

	public void deactivate() {
		active = false;
		status = DeliveryPersonStatus.UNAVAILABLE;
	}

}
