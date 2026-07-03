package com.gortona.logitrack.mapper;

import com.gortona.logitrack.dto.order.CreateOrderRequest;
import com.gortona.logitrack.dto.order.OrderResponse;
import com.gortona.logitrack.entity.Client;
import com.gortona.logitrack.entity.Delivery;
import com.gortona.logitrack.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderMapper {

	public Order toEntity(CreateOrderRequest request, Client client) {
		return Order.create(
				null,
				client,
				request.pickupAddress().trim(),
				request.deliveryAddress().trim(),
				request.description().trim()
		);
	}

	public OrderResponse toResponse(Order order, Delivery delivery) {
		return new OrderResponse(
				order.getId(),
				order.getTrackingCode(),
				order.getClient().getId(),
				order.getClient().getCode(),
				order.getClient().getName(),
				order.getPickupAddress(),
				order.getDeliveryAddress(),
				order.getDescription(),
				delivery.getId(),
				delivery.getStatus(),
				Objects.nonNull(delivery.getDeliveryPerson()) ? delivery.getDeliveryPerson().getId() : null,
				Objects.nonNull(delivery.getDeliveryPerson()) ? delivery.getDeliveryPerson().getCode() : null,
				Objects.nonNull(delivery.getVehicle()) ? delivery.getVehicle().getId() : null,
				Objects.nonNull(delivery.getVehicle()) ? delivery.getVehicle().getCode() : null,
				order.getCreatedAt(),
				delivery.getUpdatedAt()
		);
	}
}
