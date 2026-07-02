package com.gortona.logitrack.service;

import com.gortona.logitrack.repository.ClientRepository;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import com.gortona.logitrack.repository.OrderRepository;
import com.gortona.logitrack.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class FriendlyCodeService {

	private final ClientRepository clientRepository;
	private final DeliveryPersonRepository deliveryPersonRepository;
	private final VehicleRepository vehicleRepository;
	private final OrderRepository orderRepository;

	public String nextClientCode() {
		return nextSequentialCode("CLI", clientRepository.count() + 1, clientRepository::existsByCode);
	}

	public String nextDeliveryPersonCode() {
		return nextSequentialCode("ENT", deliveryPersonRepository.count() + 1, deliveryPersonRepository::existsByCode);
	}

	public String nextVehicleCode() {
		return nextSequentialCode("VEI", vehicleRepository.count() + 1, vehicleRepository::existsByCode);
	}

	public String nextOrderTrackingCode() {
		int year = Year.now().getValue();
		long sequence = orderRepository.countByTrackingCodeStartingWith("PED-" + year + "-") + 1;
		String code;
		do {
			code = "PED-%d-%04d".formatted(year, sequence++);
		} while (orderRepository.existsByTrackingCode(code));
		return code;
	}

	private String nextSequentialCode(String prefix, long initialSequence, CodeExistsPredicate existsPredicate) {
		long sequence = initialSequence;
		String code;
		do {
			code = "%s-%04d".formatted(prefix, sequence++);
		} while (existsPredicate.exists(code));
		return code;
	}

	@FunctionalInterface
	private interface CodeExistsPredicate {
		boolean exists(String code);
	}
}
