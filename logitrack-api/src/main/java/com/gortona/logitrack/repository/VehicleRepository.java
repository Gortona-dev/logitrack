package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.Vehicle;
import com.gortona.logitrack.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

	boolean existsByLicensePlate(String licensePlate);

	boolean existsByCode(String code);

	boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);

	Optional<Vehicle> findByLicensePlate(String licensePlate);

	List<Vehicle> findByActiveTrue();

	Page<Vehicle> findByActiveTrue(Pageable pageable);

	@Query("""
			select v from Vehicle v
			where v.active = true
			and (
				lower(v.code) like lower(concat('%', :search, '%'))
				or lower(v.licensePlate) like lower(concat('%', :search, '%'))
				or lower(v.brand) like lower(concat('%', :search, '%'))
				or lower(v.model) like lower(concat('%', :search, '%'))
				or lower(cast(v.status as string)) like lower(concat('%', :search, '%'))
			)
			""")
	Page<Vehicle> searchActive(String search, Pageable pageable);

	Optional<Vehicle> findByIdAndActiveTrue(UUID id);

	long countByActiveTrue();

	long countByActiveFalse();

	long countByStatusAndActiveTrue(VehicleStatus status);
}
