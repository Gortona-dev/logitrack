package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.common.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
		return findPage(clientId, deliveryPersonId, status, "", 0, 100).content();
	}

	@Transactional(readOnly = true)
	public PageResponse<OrderResponse> findPage(
			UUID clientId,
			UUID deliveryPersonId,
			DeliveryStatus status,
			String search,
			int page,
			int size
	) {
		AppUser user = currentUserService.getAuthenticatedUser();
		int safePage = Math.max(page, 0);
		int safeSize = Math.clamp(size, 1, 50);
		PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("updatedAt").descending());
		UUID effectiveClientId = clientId;
		UUID effectiveDeliveryPersonId = deliveryPersonId;

		if (user.getRole() == Role.CLIENTE) {
			if (user.getClientId() == null) {
				return emptyPage(safePage, safeSize);
			}
			effectiveClientId = user.getClientId();
		}

		if (user.getRole() == Role.ENTREGADOR) {
			if (user.getDeliveryPersonId() == null) {
				return emptyPage(safePage, safeSize);
			}
			effectiveDeliveryPersonId = user.getDeliveryPersonId();
		}

		String normalizedSearch = search == null ? "" : search.trim();
		Page<OrderResponse> orders = deliveryRepository
				.searchOrders(effectiveClientId, effectiveDeliveryPersonId, status, normalizedSearch, pageRequest)
				.map(delivery -> orderMapper.toResponse(delivery.getOrder(), delivery));

		return PageResponse.from(orders);
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

	private PageResponse<OrderResponse> emptyPage(int page, int size) {
		return new PageResponse<>(List.of(), page, size, 0, 0, true, true);
	}
}
