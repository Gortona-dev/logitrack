package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.order.CreateOrderRequest;
import com.gortona.logitrack.dto.order.OrderResponse;
import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.entity.Client;
import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.entity.DeliveryStatusHistory;
import com.gortona.logitrack.entity.Order;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.enums.Role;
import com.gortona.logitrack.exception.BusinessRuleException;
import com.gortona.logitrack.exception.ResourceNotFoundException;
import com.gortona.logitrack.mapper.OrderMapper;
import com.gortona.logitrack.repository.ClientRepository;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.DeliveryStatusHistoryRepository;
import com.gortona.logitrack.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final ClientRepository clientRepository;
	private final DeliveryRepository deliveryRepository;
	private final DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;
	private final OrderMapper orderMapper;
	private final CurrentUserService currentUserService;
	private final FriendlyCodeService friendlyCodeService;

	@Transactional
	public OrderResponse create(CreateOrderRequest request) {
		AppUser user = currentUserService.getAuthenticatedUser();
		UUID targetClientId = user.getRole() == Role.CLIENTE ? user.getClientId() : request.clientId();

		if (targetClientId == null) {
			throw new BusinessRuleException("Usuário cliente não está vinculado a um cadastro de cliente");
		}

		Client client = clientRepository.findByIdAndDeletedAtIsNull(targetClientId)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

		Order order = orderMapper.toEntity(request, client);
		order.setTrackingCode(friendlyCodeService.nextOrderTrackingCode());
		Order savedOrder = orderRepository.save(order);

		Delivery delivery = Delivery.createPending(savedOrder);
		Delivery savedDelivery = deliveryRepository.save(delivery);
		deliveryStatusHistoryRepository.save(
				DeliveryStatusHistory.create(savedDelivery, null, DeliveryStatus.PENDING, "Pedido criado")
		);

		return orderMapper.toResponse(savedOrder, savedDelivery);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> findAll(UUID clientId, UUID deliveryPersonId, DeliveryStatus status) {
		AppUser user = currentUserService.getAuthenticatedUser();

		return deliveryRepository.findAll()
				.stream()
				.filter(delivery -> canViewDelivery(user, delivery))
				.filter(delivery -> Objects.isNull(clientId) || delivery.getOrder().getClient().getId().equals(clientId))
				.filter(delivery -> Objects.isNull(deliveryPersonId)
						|| (Objects.nonNull(delivery.getDeliveryPerson())
						&& delivery.getDeliveryPerson().getId().equals(deliveryPersonId)))
				.filter(delivery -> Objects.isNull(status) || delivery.getStatus() == status)
				.map(delivery -> orderMapper.toResponse(delivery.getOrder(), delivery))
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(UUID id) {
		AppUser user = currentUserService.getAuthenticatedUser();
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
		Delivery delivery = findDeliveryByOrderId(order.getId());

		if (!canViewDelivery(user, delivery)) {
			throw new ResourceNotFoundException("Pedido não encontrado");
		}

		return orderMapper.toResponse(order, delivery);
	}

	private boolean canViewDelivery(AppUser user, Delivery delivery) {
		if (user.getRole() == Role.ADMIN || user.getRole() == Role.OPERADOR) {
			return true;
		}

		if (user.getRole() == Role.CLIENTE) {
			return user.getClientId() != null && delivery.getOrder().getClient().getId().equals(user.getClientId());
		}

		if (user.getRole() == Role.ENTREGADOR) {
			return user.getDeliveryPersonId() != null
					&& delivery.getDeliveryPerson() != null
					&& delivery.getDeliveryPerson().getId().equals(user.getDeliveryPersonId());
		}

		return false;
	}

	private Delivery findDeliveryByOrderId(UUID orderId) {
		return deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Entrega não encontrada para o pedido"));
	}
}
