package com.gortona.logitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class LogitrackApiApplication {

	public static void main(String[] args) {
		configureRenderDatabaseUrl();
		SpringApplication.run(LogitrackApiApplication.class, args);
	}

	private static void configureRenderDatabaseUrl() {
		String databaseUrl = System.getenv("DATABASE_URL");
		String explicitJdbcUrl = System.getenv("DB_URL");

		if (explicitJdbcUrl != null || databaseUrl == null || databaseUrl.isBlank()) {
			return;
		}

		URI uri = URI.create(databaseUrl);
		String[] credentials = uri.getUserInfo().split(":", 2);
		String port = uri.getPort() > 0 ? ":" + uri.getPort() : "";
		String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + port + uri.getPath();

		System.setProperty("spring.datasource.url", jdbcUrl);
		System.setProperty("spring.datasource.username", decode(credentials[0]));
		System.setProperty("spring.datasource.password", credentials.length > 1 ? decode(credentials[1]) : "");
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

}
