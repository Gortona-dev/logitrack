package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.enums.DeliveryPersonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

	Page<DeliveryPerson> findByActiveTrue(Pageable pageable);

	@Query("""
			select d from DeliveryPerson d
			where d.active = true
			and (
				lower(d.name) like lower(concat('%', :search, '%'))
				or lower(d.code) like lower(concat('%', :search, '%'))
				or lower(d.email) like lower(concat('%', :search, '%'))
				or d.document like concat('%', :search, '%')
				or d.phone like concat('%', :search, '%')
			)
			""")
	Page<DeliveryPerson> searchActive(String search, Pageable pageable);

	List<DeliveryPerson> findByStatus(DeliveryPersonStatus status);

	long countByStatus(DeliveryPersonStatus status);
}
