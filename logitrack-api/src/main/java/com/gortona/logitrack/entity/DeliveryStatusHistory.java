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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "delivery_status_history")
public class DeliveryStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "delivery_id", nullable = false)
	private Delivery delivery;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private DeliveryStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DeliveryStatus newStatus;

	@Column(length = 500)
	private String notes;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime changedAt;

	public static DeliveryStatusHistory create(
			Delivery delivery,
			DeliveryStatus previousStatus,
			DeliveryStatus newStatus,
			String notes
	) {
		DeliveryStatusHistory history = new DeliveryStatusHistory();
		history.delivery = delivery;
		history.previousStatus = previousStatus;
		history.newStatus = newStatus;
		history.notes = notes;
		return history;
	}

}
