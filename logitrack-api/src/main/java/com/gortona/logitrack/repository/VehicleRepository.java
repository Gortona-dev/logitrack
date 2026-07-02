package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.Vehicle;
import com.gortona.logitrack.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

	boolean existsByLicensePlate(String licensePlate);

	boolean existsByCode(String code);

	boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);

	Optional<Vehicle> findByLicensePlate(String licensePlate);

	List<Vehicle> findByActiveTrue();

	Optional<Vehicle> findByIdAndActiveTrue(UUID id);

	long countByActiveTrue();

	long countByActiveFalse();

	long countByStatusAndActiveTrue(VehicleStatus status);
}
