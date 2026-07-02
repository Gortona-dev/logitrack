package com.gortona.logitrack.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final byte[] secret;
	private final long expirationSeconds;

	public JwtService(
			@Value("${app.security.jwt.secret}") String secret,
			@Value("${app.security.jwt.expiration-seconds}") long expirationSeconds
	) {
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(AppUser user) {
		Instant now = Instant.now();
		Map<String, Object> header = Map.of(
				"alg", "HS256",
				"typ", "JWT"
		);
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getEmail());
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", now.plusSeconds(expirationSeconds).getEpochSecond());

		String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
		return unsignedToken + "." + sign(unsignedToken);
	}

	public String extractSubject(String token) {
		return readPayload(token).get("sub").toString();
	}

	public Role extractRole(String token) {
		return Role.valueOf(readPayload(token).get("role").toString());
	}

	public boolean isValid(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				return false;
			}

			String unsignedToken = parts[0] + "." + parts[1];
			boolean signatureMatches = constantTimeEquals(sign(unsignedToken), parts[2]);
			if (!signatureMatches) {
				return false;
			}

			Number expiresAt = (Number) readPayload(token).get("exp");
			return Instant.now().getEpochSecond() < expiresAt.longValue();
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private Map<String, Object> readPayload(String token) {
		try {
			String[] parts = token.split("\\.");
			byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
			return objectMapper.readValue(payload, MAP_TYPE);
		} catch (Exception exception) {
			throw new IllegalArgumentException("Token JWT inválido", exception);
		}
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return Base64.getUrlEncoder()
					.withoutPadding()
					.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (Exception exception) {
			throw new IllegalStateException("Não foi possível gerar o token JWT", exception);
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
			return Base64.getUrlEncoder()
					.withoutPadding()
					.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Não foi possível assinar o token JWT", exception);
		}
	}

	private boolean constantTimeEquals(String expected, String actual) {
		return java.security.MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8)
		);
	}
}
