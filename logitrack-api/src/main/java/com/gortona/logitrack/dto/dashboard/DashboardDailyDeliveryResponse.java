package com.gortona.logitrack.dto.dashboard;

import java.time.LocalDate;

public record DashboardDailyDeliveryResponse(
		LocalDate date,
		long delivered
) {
}
