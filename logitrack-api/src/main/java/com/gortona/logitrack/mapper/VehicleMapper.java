package com.gortona.logitrack.mapper;

import com.gortona.logitrack.dto.vehicle.CreateVehicleRequest;
import com.gortona.logitrack.dto.vehicle.VehicleResponse;
import com.gortona.logitrack.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

	public Vehicle toEntity(CreateVehicleRequest request) {
		return Vehicle.create(
				null,
				request.licensePlate().trim().toUpperCase(),
				request.brand().trim(),
				request.model().trim()
		);
	}

	public VehicleResponse toResponse(Vehicle vehicle) {
		return new VehicleResponse(
				vehicle.getId(),
				vehicle.getCode(),
				vehicle.getLicensePlate(),
				vehicle.getBrand(),
				vehicle.getModel(),
				vehicle.isActive(),
				vehicle.getStatus(),
				vehicle.getCreatedAt(),
				vehicle.getUpdatedAt()
		);
	}
}
