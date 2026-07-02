package com.gortona.logitrack.dto.dashboard;

import java.util.List;

public record DashboardResponse(
		long pending,
		long assigned,
		long inTransit,
		long deliveredToday,
		long cancelled,
		long driversAvailable,
		long driversBusy,
		long activeVehicles,
		long vehiclesInMaintenance,
		List<DashboardDeliveryResponse> latestDeliveries
) {
}
