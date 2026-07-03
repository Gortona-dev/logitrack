package com.gortona.logitrack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gortona.logitrack.dto.dashboard.DashboardDailyDeliveryResponse;
import com.gortona.logitrack.dto.dashboard.DashboardDeliveryResponse;
import com.gortona.logitrack.dto.dashboard.DashboardResponse;
import com.gortona.logitrack.dto.dashboard.DashboardStatusCountResponse;
import com.gortona.logitrack.enums.DeliveryStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
	private static final TypeReference<List<Map<String, Object>>> JSON_LIST_TYPE = new TypeReference<>() {
	};
	private static final String DASHBOARD_SQL = """
			WITH params AS (
				SELECT CAST(?1 AS date) AS today
			),
			delivery_counts AS (
				SELECT
					count(*) AS total_deliveries,
					count(*) FILTER (WHERE status = 'PENDING') AS pending,
					count(*) FILTER (WHERE status = 'ASSIGNED') AS assigned,
					count(*) FILTER (WHERE status = 'PICKED_UP') AS picked_up,
					count(*) FILTER (WHERE status = 'IN_TRANSIT') AS in_transit,
					count(*) FILTER (WHERE status = 'DELIVERED') AS delivered,
					count(*) FILTER (
						WHERE status = 'DELIVERED'
						AND (updated_at AT TIME ZONE 'America/Sao_Paulo')::date = (SELECT today FROM params)
					) AS delivered_today,
					count(*) FILTER (WHERE status = 'CANCELLED') AS cancelled
				FROM deliveries
			),
			order_counts AS (
				SELECT count(*) AS total_orders FROM orders
			),
			person_counts AS (
				SELECT
					count(*) FILTER (WHERE status = 'AVAILABLE') AS drivers_available,
					count(*) FILTER (WHERE status = 'ON_DELIVERY') AS drivers_busy,
					count(*) FILTER (WHERE status = 'UNAVAILABLE') AS drivers_inactive
				FROM delivery_persons
				WHERE active = true
			),
			vehicle_counts AS (
				SELECT
					count(*) FILTER (WHERE active = true) AS active_vehicles,
					count(*) FILTER (WHERE active = true AND status = 'AVAILABLE') AS vehicles_available,
					count(*) FILTER (WHERE active = true AND status = 'IN_USE') AS vehicles_in_use,
					count(*) FILTER (WHERE active = true AND status = 'MAINTENANCE') AS vehicles_in_maintenance
				FROM vehicles
			),
			status_distribution AS (
				SELECT jsonb_build_array(
					jsonb_build_object('status', 'PENDING', 'total', pending),
					jsonb_build_object('status', 'ASSIGNED', 'total', assigned),
					jsonb_build_object('status', 'PICKED_UP', 'total', picked_up),
					jsonb_build_object('status', 'IN_TRANSIT', 'total', in_transit),
					jsonb_build_object('status', 'DELIVERED', 'total', delivered),
					jsonb_build_object('status', 'CANCELLED', 'total', cancelled)
				) AS data
				FROM delivery_counts
			),
			daily_rows AS (
				SELECT
					series.day::date AS day,
					count(d.id) AS delivered
				FROM params p
				CROSS JOIN generate_series(p.today - INTERVAL '6 days', p.today, INTERVAL '1 day') AS series(day)
				LEFT JOIN deliveries d
					ON d.status = 'DELIVERED'
					AND (d.updated_at AT TIME ZONE 'America/Sao_Paulo')::date = series.day::date
				GROUP BY series.day
				ORDER BY series.day
			),
			daily_distribution AS (
				SELECT coalesce(
					jsonb_agg(jsonb_build_object('date', day::text, 'delivered', delivered) ORDER BY day),
					'[]'::jsonb
				) AS data
				FROM daily_rows
			),
			latest_rows AS (
				SELECT
					o.id::text AS order_id,
					c.name AS client_name,
					o.delivery_address,
					d.status,
					d.updated_at,
					to_char(d.updated_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"') AS updated_at_iso
				FROM deliveries d
				JOIN orders o ON o.id = d.order_id
				JOIN clients c ON c.id = o.client_id
				ORDER BY d.updated_at DESC
				LIMIT 5
			),
			latest_distribution AS (
				SELECT coalesce(
					jsonb_agg(jsonb_build_object(
						'orderId', order_id,
						'clientName', client_name,
						'deliveryAddress', delivery_address,
						'status', status,
						'updatedAt', updated_at_iso
					) ORDER BY updated_at DESC),
					'[]'::jsonb
				) AS data
				FROM latest_rows
			)
			SELECT
				oc.total_orders,
				dc.total_deliveries,
				dc.pending,
				dc.assigned,
				dc.picked_up,
				dc.in_transit,
				dc.delivered,
				dc.delivered_today,
				dc.cancelled,
				pc.drivers_available,
				pc.drivers_busy,
				pc.drivers_inactive,
				vc.active_vehicles,
				vc.vehicles_available,
				vc.vehicles_in_use,
				vc.vehicles_in_maintenance,
				sd.data AS status_distribution,
				dd.data AS delivered_last_seven_days,
				ld.data AS latest_deliveries
			FROM delivery_counts dc
			CROSS JOIN order_counts oc
			CROSS JOIN person_counts pc
			CROSS JOIN vehicle_counts vc
			CROSS JOIN status_distribution sd
			CROSS JOIN daily_distribution dd
			CROSS JOIN latest_distribution ld
			""";

	private final EntityManager entityManager;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Transactional(readOnly = true)
	public DashboardResponse getOverview() {
		Query query = entityManager.createNativeQuery(DASHBOARD_SQL);
		query.setParameter(1, LocalDate.now(BUSINESS_ZONE));
		Object[] row = (Object[]) query.getSingleResult();

		return new DashboardResponse(
				longValue(row, 0),
				longValue(row, 1),
				longValue(row, 2),
				longValue(row, 3),
				longValue(row, 4),
				longValue(row, 5),
				longValue(row, 6),
				longValue(row, 7),
				longValue(row, 8),
				longValue(row, 9),
				longValue(row, 10),
				longValue(row, 11),
				longValue(row, 12),
				longValue(row, 13),
				longValue(row, 14),
				longValue(row, 15),
				mapStatusDistribution(jsonValue(row, 16)),
				mapDailyDeliveries(jsonValue(row, 17)),
				mapLatestDeliveries(jsonValue(row, 18))
		);
	}

	private List<DashboardStatusCountResponse> mapStatusDistribution(String json) {
		return readJsonList(json).stream()
				.map(item -> new DashboardStatusCountResponse(
						DeliveryStatus.valueOf(stringValue(item, "status")),
						numberValue(item, "total")
				))
				.toList();
	}

	private List<DashboardDailyDeliveryResponse> mapDailyDeliveries(String json) {
		return readJsonList(json).stream()
				.map(item -> new DashboardDailyDeliveryResponse(
						LocalDate.parse(stringValue(item, "date")),
						numberValue(item, "delivered")
				))
				.toList();
	}

	private List<DashboardDeliveryResponse> mapLatestDeliveries(String json) {
		return readJsonList(json).stream()
				.map(item -> new DashboardDeliveryResponse(
						UUID.fromString(stringValue(item, "orderId")),
						stringValue(item, "clientName"),
						stringValue(item, "deliveryAddress"),
						DeliveryStatus.valueOf(stringValue(item, "status")),
						OffsetDateTime.parse(stringValue(item, "updatedAt"))
				))
				.toList();
	}

	private List<Map<String, Object>> readJsonList(String json) {
		try {
			return objectMapper.readValue(json, JSON_LIST_TYPE);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Falha ao montar os dados do dashboard.", exception);
		}
	}

	private String jsonValue(Object[] row, int index) {
		Object value = row[index];
		return value == null ? "[]" : value.toString();
	}

	private String stringValue(Map<String, Object> item, String key) {
		Object value = item.get(key);
		return value == null ? "" : value.toString();
	}

	private long numberValue(Map<String, Object> item, String key) {
		Object value = item.get(key);
		return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
	}

	private long longValue(Object[] row, int index) {
		Object value = row[index];
		return value instanceof Number number ? number.longValue() : 0L;
	}
}
