package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.enums.DeliveryPersonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, UUID> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, UUID id);

	boolean existsByDocument(String document);

	boolean existsByCode(String code);

	boolean existsByDocumentAndIdNot(String document, UUID id);

	Optional<DeliveryPerson> findByEmail(String email);

	Optional<DeliveryPerson> findByCode(String code);

	List<DeliveryPerson> findByActiveTrue();

	List<DeliveryPerson> findByStatus(DeliveryPersonStatus status);

	long countByStatus(DeliveryPersonStatus status);
}
