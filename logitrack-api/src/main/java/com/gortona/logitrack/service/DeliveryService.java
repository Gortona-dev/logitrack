package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.delivery.AssignDeliveryRequest;
import com.gortona.logitrack.dto.delivery.DeliveryResponse;
import com.gortona.logitrack.dto.delivery.DeliveryStatusHistoryResponse;
import com.gortona.logitrack.dto.delivery.UpdateDeliveryStatusRequest;
import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.entity.DeliveryPerson;
import com.gortona.logitrack.entity.DeliveryStatusHistory;
import com.gortona.logitrack.entity.Vehicle;
import com.gortona.logitrack.enums.DeliveryPersonStatus;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.enums.Role;
import com.gortona.logitrack.exception.BusinessRuleException;
import com.gortona.logitrack.exception.ResourceNotFoundException;
import com.gortona.logitrack.mapper.DeliveryMapper;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.DeliveryStatusHistoryRepository;
import com.gortona.logitrack.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private static final List<DeliveryStatus> ACTIVE_DELIVERY_STATUSES = List.of(
			DeliveryStatus.ASSIGNED,
			DeliveryStatus.PICKED_UP,
			DeliveryStatus.IN_TRANSIT
	);

	private final DeliveryRepository deliveryRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final VehicleRepository vehicleRepository;
	private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;
	private final DeliveryMapper deliveryMapper;
	private final CurrentUserService currentUserService;

	@Transactional
	public DeliveryResponse assign(UUID orderId, AssignDeliveryRequest request) {
		Delivery delivery = deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Entrega não encontrada para o pedido"));

		if (delivery.getStatus() != DeliveryStatus.PENDING) {
			throw new BusinessRuleException("Apenas pedidos pendentes podem receber um entregador");
		}

		DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(request.deliveryPersonId())
				.orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado"));

		if (deliveryPerson.getStatus() != DeliveryPersonStatus.AVAILABLE) {
			throw new BusinessRuleException("O entregador deve estar disponível");
		}

		Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
				.orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

		if (!vehicle.isActive()) {
			throw new BusinessRuleException("O veículo deve estar ativo");
		}

		if (deliveryRepository.existsByVehicleIdAndStatusIn(vehicle.getId(), ACTIVE_DELIVERY_STATUSES)) {
			throw new BusinessRuleException("O veículo já está em uso");
		}

		DeliveryStatus previousStatus = delivery.getStatus();
		delivery.assign(deliveryPerson, vehicle);
		deliveryPerson.setStatus(DeliveryPersonStatus.ON_DELIVERY);
		vehicle.markInUse();

		Delivery savedDelivery = deliveryRepository.save(delivery);
		deliveryStatusHistoryRepository.save(
				DeliveryStatusHistory.create(savedDelivery, previousStatus, DeliveryStatus.ASSIGNED, "Entrega atribuída")
		);

		return deliveryMapper.toResponse(savedDelivery);
	}

	@Transactional
	public DeliveryResponse updateStatus(UUID deliveryId, UpdateDeliveryStatusRequest request) {
		AppUser user = currentUserService.getAuthenticatedUser();
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new ResourceNotFoundException("Entrega não encontrada"));

		if (!canUpdateDelivery(user, delivery)) {
			throw new ResourceNotFoundException("Entrega não encontrada");
		}

		validateStatusChange(delivery, request.status());

		DeliveryStatus previousStatus = delivery.getStatus();
		delivery.changeStatus(request.status());

		if (request.status() == DeliveryStatus.DELIVERED || request.status() == DeliveryStatus.CANCELLED) {
			releaseDeliveryPerson(delivery);
			releaseVehicleIfPossible(delivery);
		}

		Delivery savedDelivery = deliveryRepository.save(delivery);
		deliveryStatusHistoryRepository.save(
				DeliveryStatusHistory.create(savedDelivery, previousStatus, request.status(), normalizeNotes(request.notes()))
		);

		return deliveryMapper.toResponse(savedDelivery);
	}

	@Transactional(readOnly = true)
	public List<DeliveryStatusHistoryResponse> findHistory(UUID deliveryId) {
		AppUser user = currentUserService.getAuthenticatedUser();
		Delivery delivery = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new ResourceNotFoundException("Entrega não encontrada"));

		if (!canViewDelivery(user, delivery)) {
			throw new ResourceNotFoundException("Entrega não encontrada");
		}

		return deliveryStatusHistoryRepository.findByDeliveryIdOrderByChangedAtAsc(deliveryId)
				.stream()
				.map(deliveryMapper::toHistoryResponse)
				.toList();
	}

	private boolean canViewDelivery(AppUser user, Delivery delivery) {
		if (user.getRole() == Role.ADMIN || user.getRole() == Role.OPERADOR) {
			return true;
		}

		if (user.getRole() == Role.CLIENTE) {
			return user.getClientId() != null && delivery.getOrder().getClient().getId().equals(user.getClientId());
		}

		return canUpdateDelivery(user, delivery);
	}

	private boolean canUpdateDelivery(AppUser user, Delivery delivery) {
		if (user.getRole() == Role.ADMIN || user.getRole() == Role.OPERADOR) {
			return true;
		}

		return user.getRole() == Role.ENTREGADOR
				&& user.getDeliveryPersonId() != null
				&& delivery.getDeliveryPerson() != null
				&& delivery.getDeliveryPerson().getId().equals(user.getDeliveryPersonId());
	}

	private void validateStatusChange(Delivery delivery, DeliveryStatus newStatus) {
		if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
			throw new BusinessRuleException("Um pedido entregue não pode ser alterado");
		}

		if (delivery.getStatus() == newStatus) {
			throw new BusinessRuleException("A entrega já possui este status");
		}

		if (newStatus == DeliveryStatus.PENDING) {
			throw new BusinessRuleException("A entrega não pode retornar ao status pendente");
		}

		if (newStatus == DeliveryStatus.ASSIGNED) {
			throw new BusinessRuleException("Use o endpoint de atribuição para atribuir uma entrega");
		}

		if (newStatus == DeliveryStatus.PICKED_UP && delivery.getStatus() != DeliveryStatus.ASSIGNED) {
			throw new BusinessRuleException("A entrega deve ser atribuída antes da coleta");
		}

		if (newStatus == DeliveryStatus.IN_TRANSIT && delivery.getStatus() != DeliveryStatus.PICKED_UP) {
			throw new BusinessRuleException("A entrega deve ser coletada antes de entrar em trânsito");
		}

		if (newStatus == DeliveryStatus.DELIVERED && delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
			throw new BusinessRuleException("A entrega deve estar em trânsito antes de ser entregue");
		}

		if (newStatus == DeliveryStatus.CANCELLED && delivery.getStatus() == DeliveryStatus.DELIVERED) {
			throw new BusinessRuleException("Um pedido entregue não pode ser cancelado");
		}
	}

	private void releaseDeliveryPerson(Delivery delivery) {
		if (delivery.getDeliveryPerson() != null) {
			delivery.getDeliveryPerson().setStatus(DeliveryPersonStatus.AVAILABLE);
		}
	}

	private void releaseVehicleIfPossible(Delivery delivery) {
		if (delivery.getVehicle() == null) {
			return;
		}

		boolean vehicleHasActiveDelivery = deliveryRepository.existsByVehicleIdAndStatusIn(
				delivery.getVehicle().getId(),
				ACTIVE_DELIVERY_STATUSES
		);

		if (!vehicleHasActiveDelivery && delivery.getVehicle().isActive()) {
			delivery.getVehicle().markAvailable();
		}
	}

	private String normalizeNotes(String notes) {
		return notes == null || notes.isBlank() ? null : notes.trim();
	}
}
