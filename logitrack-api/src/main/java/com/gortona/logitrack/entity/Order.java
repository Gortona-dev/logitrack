package com.gortona.logitrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@Column(nullable = false, length = 255)
	private String pickupAddress;

	@Column(nullable = false, length = 255)
	private String deliveryAddress;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false, unique = true, length = 30)
	private String trackingCode;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	public static Order create(String trackingCode, Client client, String pickupAddress, String deliveryAddress, String description) {
		Order order = new Order();
		order.trackingCode = trackingCode;
		order.client = client;
		order.pickupAddress = pickupAddress;
		order.deliveryAddress = deliveryAddress;
		order.description = description;
		return order;
	}

}
