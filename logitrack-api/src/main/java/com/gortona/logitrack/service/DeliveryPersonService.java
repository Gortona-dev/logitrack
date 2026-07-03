package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.common.PageResponse;
import com.gortona.logitrack.dto.deliveryperson.CreateDeliveryPersonRequest;
import com.gortona.logitrack.dto.deliveryperson.DeliveryPersonResponse;
import com.gortona.logitrack.dto.deliveryperson.UpdateDeliveryPersonRequest;
import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.exception.BusinessRuleException;
import com.gortona.logitrack.exception.ConflictException;
import com.gortona.logitrack.exception.ResourceNotFoundException;
import com.gortona.logitrack.mapper.DeliveryPersonMapper;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryPersonService {

	private static final List<DeliveryStatus> ACTIVE_DELIVERY_STATUSES = List.of(
			DeliveryStatus.ASSIGNED,
			DeliveryStatus.PICKED_UP,
			DeliveryStatus.IN_TRANSIT
	);

	private final DeliveryPersonRepository deliveryPersonRepository;
	private final DeliveryRepository deliveryRepository;
	private final DeliveryPersonMapper deliveryPersonMapper;
	private final FriendlyCodeService friendlyCodeService;

	@Transactional
	public DeliveryPersonResponse create(CreateDeliveryPersonRequest request) {
		validateUniqueDeliveryPerson(request);

		DeliveryPerson deliveryPerson = deliveryPersonMapper.toEntity(request);
		deliveryPerson.setCode(friendlyCodeService.nextDeliveryPersonCode());
		DeliveryPerson savedDeliveryPerson = deliveryPersonRepository.save(deliveryPerson);

		return deliveryPersonMapper.toResponse(savedDeliveryPerson);
	}

	@Transactional(readOnly = true)
	public List<DeliveryPersonResponse> findAll() {
		return deliveryPersonRepository.findByActiveTrue()
				.stream()
				.map(deliveryPerson -> deliveryPersonMapper.toResponse(deliveryPerson, findAssignedVehicle(deliveryPerson)))
				.toList();
	}

	@Transactional(readOnly = true)
	public PageResponse<DeliveryPersonResponse> findPage(String search, int page, int size) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.clamp(size, 1, 50);
		PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
		String normalizedSearch = search == null ? "" : search.trim();

		if (normalizedSearch.isBlank()) {
			return PageResponse.from(deliveryPersonRepository.findByActiveTrue(pageRequest)
					.map(deliveryPerson -> deliveryPersonMapper.toResponse(deliveryPerson, findAssignedVehicle(deliveryPerson))));
		}

		return PageResponse.from(deliveryPersonRepository.searchActive(normalizedSearch, pageRequest)
				.map(deliveryPerson -> deliveryPersonMapper.toResponse(deliveryPerson, findAssignedVehicle(deliveryPerson))));
	}

	@Transactional
	public DeliveryPersonResponse update(UUID id, UpdateDeliveryPersonRequest request) {
		DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(id)
				.filter(candidate -> candidate.isActive())
				.orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado"));
		String email = request.email().trim().toLowerCase();
		String document = request.document().trim();

		if (deliveryPersonRepository.existsByEmailAndIdNot(email, id)) {
			throw new ConflictException("Já existe um entregador com este email");
		}

		if (deliveryPersonRepository.existsByDocumentAndIdNot(document, id)) {
			throw new ConflictException("Já existe um entregador com este documento");
		}

		deliveryPerson.setName(request.name().trim());
		deliveryPerson.setEmail(email);
		deliveryPerson.setDocument(document);
		deliveryPerson.setPhone(request.phone().trim());

		DeliveryPerson savedDeliveryPerson = deliveryPersonRepository.save(deliveryPerson);
		return deliveryPersonMapper.toResponse(savedDeliveryPerson, findAssignedVehicle(savedDeliveryPerson));
	}

	@Transactional
	public void delete(UUID id) {
		DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(id)
				.filter(candidate -> candidate.isActive())
				.orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado"));

		if (deliveryRepository.existsByDeliveryPersonIdAndStatusIn(id, ACTIVE_DELIVERY_STATUSES)) {
			throw new BusinessRuleException("Não é possível excluir entregador em entrega ativa");
		}

		deliveryPerson.deactivate();
		deliveryPersonRepository.save(deliveryPerson);
	}

	private void validateUniqueDeliveryPerson(CreateDeliveryPersonRequest request) {
		String email = request.email().trim().toLowerCase();
		String document = request.document().trim();

		if (deliveryPersonRepository.existsByEmail(email)) {
			throw new ConflictException("Já existe um entregador com este email");
		}

		if (deliveryPersonRepository.existsByDocument(document)) {
			throw new ConflictException("Já existe um entregador com este documento");
		}
	}

	private String findAssignedVehicle(DeliveryPerson deliveryPerson) {
		return deliveryRepository.findByDeliveryPersonId(deliveryPerson.getId())
				.stream()
				.filter(delivery -> ACTIVE_DELIVERY_STATUSES.contains(delivery.getStatus()))
				.map(delivery -> delivery.getVehicle())
				.filter(vehicle -> vehicle != null)
				.findFirst()
				.map(vehicle -> vehicle.getLicensePlate() + " - " + vehicle.getModel())
				.orElse(null);
	}
}
