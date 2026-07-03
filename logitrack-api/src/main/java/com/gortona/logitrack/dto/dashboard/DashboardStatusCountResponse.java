package com.gortona.logitrack.dto.dashboard;

import com.gortona.logitrack.enums.DeliveryStatus;

public record DashboardStatusCountResponse(
		DeliveryStatus status,
		long total
) {
}
