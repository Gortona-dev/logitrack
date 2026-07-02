package com.gortona.logitrack.config;

import com.gortona.logitrack.security.JwtAuthenticationFilter;
import com.gortona.logitrack.repository.AppUserRepository;
import com.gortona.logitrack.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/v1/dashboard").hasRole("ADMIN")
						.requestMatchers("/api/v1/users/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/delivery-persons/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/vehicles/**").hasRole("ADMIN")
						.requestMatchers("/api/v1/clients/**").hasAnyRole("ADMIN", "OPERADOR", "CLIENTE")
						.requestMatchers("/api/v1/delivery-persons/**").hasAnyRole("ADMIN", "OPERADOR")
						.requestMatchers("/api/v1/vehicles/**").hasAnyRole("ADMIN", "OPERADOR")
						.requestMatchers("/api/v1/orders/**").hasAnyRole("ADMIN", "OPERADOR", "ENTREGADOR", "CLIENTE")
						.requestMatchers(HttpMethod.POST, "/api/v1/deliveries/*/assign").hasAnyRole("ADMIN", "OPERADOR")
						.requestMatchers(HttpMethod.GET, "/api/v1/deliveries/**").hasAnyRole("ADMIN", "OPERADOR", "ENTREGADOR", "CLIENTE")
						.requestMatchers("/api/v1/deliveries/**").hasAnyRole("ADMIN", "OPERADOR", "ENTREGADOR")
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(AppUserRepository appUserRepository) {
		return username -> appUserRepository.findByEmail(username.trim().toLowerCase())
				.orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
	}
}
