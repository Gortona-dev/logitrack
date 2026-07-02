package com.gortona.logitrack.repository;

import com.gortona.logitrack.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

	boolean existsByEmail(String email);

	boolean existsByDocument(String document);

	boolean existsByCode(String code);

	Optional<Client> findByEmail(String email);

	Optional<Client> findByCode(String code);

	Optional<Client> findByIdAndDeletedAtIsNull(UUID id);

	Page<Client> findByDeletedAtIsNull(Pageable pageable);

	@Query("""
			select c from Client c
			where c.deletedAt is null
			and (
				lower(c.name) like lower(concat('%', :search, '%'))
				or lower(c.code) like lower(concat('%', :search, '%'))
				or lower(c.email) like lower(concat('%', :search, '%'))
				or c.document like concat('%', :search, '%')
			)
			""")
	Page<Client> searchActive(String search, Pageable pageable);

	@Query("""
			select count(c) > 0 from Client c
			where c.email = :email
			and c.id <> :id
			""")
	boolean existsByEmailAndIdNot(String email, UUID id);

	@Query("""
			select count(c) > 0 from Client c
			where c.document = :document
			and c.id <> :id
			""")
	boolean existsByDocumentAndIdNot(String document, UUID id);
}
