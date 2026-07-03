package com.gortona.logitrack;

import jakarta.persistence.EntityManager;
import com.gortona.logitrack.repository.ClientRepository;
import com.gortona.logitrack.repository.AppUserRepository;
import com.gortona.logitrack.repository.DeliveryPersonRepository;
import com.gortona.logitrack.repository.DeliveryRepository;
import com.gortona.logitrack.repository.DeliveryStatusHistoryRepository;
import com.gortona.logitrack.repository.OrderRepository;
import com.gortona.logitrack.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LogitrackApiApplicationTests {

	@MockitoBean
	private AppUserRepository appUserRepository;

	@MockitoBean
	private ClientRepository clientRepository;

	@MockitoBean
	private DeliveryPersonRepository deliveryPersonRepository;

	@MockitoBean
	private VehicleRepository vehicleRepository;

	@MockitoBean
	private OrderRepository orderRepository;

	@MockitoBean
	private DeliveryRepository deliveryRepository;

	@MockitoBean
	private DeliveryStatusHistoryRepository deliveryStatusHistoryRepository;

	@MockitoBean
	private EntityManager entityManager;

	@Test
	void contextLoads() {
	}

}
