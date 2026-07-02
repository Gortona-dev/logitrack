package com.gortona.logitrack.service;

import com.gortona.logitrack.dto.vehicle.CreateVehicleRequest;
import com.gortona.logitrack.dto.vehicle.UpdateVehicleRequest;
import com.gortona.logitrack.dto.vehicle.VehicleResponse;
import com.gortona.logitrack.entity.Vehicle;
import com.gortona.logitrack.enums.DeliveryStatus;
import com.gortona.logitrack.exception.BusinessRuleException;
import com.gortona.logitrack.exception.ConflictException;
import com.gortona.logitrack.exception.ResourceNotFoundException;
import com.gortona.logitrack.mapper.VehicleMapper;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

	private static final List<DeliveryStatus> ACTIVE_DELIVERY_STATUSES = List.of(
			DeliveryStatus.ASSIGNED,
			DeliveryStatus.PICKED_UP,
			DeliveryStatus.IN_TRANSIT
	);

	private final VehicleRepository vehicleRepository;
	private final DeliveryRepository deliveryRepository;
	private final VehicleMapper vehicleMapper;
	private final FriendlyCodeService friendlyCodeService;

	@Transactional
	public VehicleResponse create(CreateVehicleRequest request) {
		validateUniqueVehicle(request);

		Vehicle vehicle = vehicleMapper.toEntity(request);
		vehicle.setCode(friendlyCodeService.nextVehicleCode());
		Vehicle savedVehicle = vehicleRepository.save(vehicle);

		return vehicleMapper.toResponse(savedVehicle);
	}

	@Transactional(readOnly = true)
	public List<VehicleResponse> findAll() {
		return vehicleRepository.findByActiveTrue()
				.stream()
				.map(vehicleMapper::toResponse)
				.toList();
	}

	@Transactional
	public VehicleResponse update(UUID id, UpdateVehicleRequest request) {
		Vehicle vehicle = vehicleRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
		String licensePlate = request.licensePlate().trim().toUpperCase();

		if (vehicleRepository.existsByLicensePlateAndIdNot(licensePlate, id)) {
			throw new ConflictException("Já existe um veículo com esta placa");
		}

		vehicle.update(licensePlate, request.brand().trim(), request.model().trim());
		return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
	}

	@Transactional
	public void delete(UUID id) {
		Vehicle vehicle = vehicleRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

		if (deliveryRepository.existsByVehicleIdAndStatusIn(id, ACTIVE_DELIVERY_STATUSES)) {
			throw new BusinessRuleException("Não é possível excluir veículo em uso");
		}

		vehicle.deactivate();
		vehicleRepository.save(vehicle);
	}

	private void validateUniqueVehicle(CreateVehicleRequest request) {
		String licensePlate = request.licensePlate().trim().toUpperCase();

		if (vehicleRepository.existsByLicensePlate(licensePlate)) {
			throw new ConflictException("Já existe um veículo com esta placa");
		}
	}
}
