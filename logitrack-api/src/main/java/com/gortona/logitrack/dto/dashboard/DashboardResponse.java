package com.gortona.logitrack.dto.dashboard;

import java.util.List;

public record DashboardResponse(
		long totalOrders,
		long totalDeliveries,
		long pending,
		long assigned,
		long pickedUp,
		long inTransit,
		long delivered,
		long deliveredToday,
		long cancelled,
		long driversAvailable,
		long driversBusy,
		long driversInactive,
		long activeVehicles,
		long vehiclesAvailable,
		long vehiclesInUse,
		long vehiclesInMaintenance,
		List<DashboardStatusCountResponse> statusDistribution,
		List<DashboardDailyDeliveryResponse> deliveredLastSevenDays,
		List<DashboardDeliveryResponse> latestDeliveries
) {
}
