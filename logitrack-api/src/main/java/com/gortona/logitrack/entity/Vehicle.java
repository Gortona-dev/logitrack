package com.gortona.logitrack.entity;

import com.gortona.logitrack.enums.VehicleStatus;
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
@Table(name = "vehicles")
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 12)
	private String licensePlate;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(nullable = false, length = 80)
	private String brand;

	@Column(nullable = false, length = 80)
	private String model;

	@Column(nullable = false)
	private boolean active = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private VehicleStatus status = VehicleStatus.AVAILABLE;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	private OffsetDateTime deletedAt;

	public static Vehicle create(String code, String licensePlate, String brand, String model) {
		Vehicle vehicle = new Vehicle();
		vehicle.code = code;
		vehicle.licensePlate = licensePlate;
		vehicle.brand = brand;
		vehicle.model = model;
		vehicle.active = true;
		vehicle.status = VehicleStatus.AVAILABLE;
		return vehicle;
	}

	public void markInUse() {
		status = VehicleStatus.IN_USE;
	}

	public void update(String licensePlate, String brand, String model) {
		this.licensePlate = licensePlate;
		this.brand = brand;
		this.model = model;
	}

	public void markAvailable() {
		status = VehicleStatus.AVAILABLE;
	}

	public void deactivate() {
		active = false;
		status = VehicleStatus.MAINTENANCE;
		deletedAt = OffsetDateTime.now();
	}
}
