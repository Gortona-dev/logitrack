package com.gortona.logitrack.entity;

import com.gortona.logitrack.enums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "deliveries")
public class Delivery {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "delivery_person_id")
	private DeliveryPerson deliveryPerson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id")
	private Vehicle vehicle;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DeliveryStatus status = DeliveryStatus.PENDING;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	public static Delivery createPending(Order order) {
		Delivery delivery = new Delivery();
		delivery.order = order;
		delivery.status = DeliveryStatus.PENDING;
		return delivery;
	}

	public void assign(DeliveryPerson deliveryPerson, Vehicle vehicle) {
		this.deliveryPerson = deliveryPerson;
		this.vehicle = vehicle;
		this.status = DeliveryStatus.ASSIGNED;
	}

	public void changeStatus(DeliveryStatus status) {
		this.status = status;
	}

}
