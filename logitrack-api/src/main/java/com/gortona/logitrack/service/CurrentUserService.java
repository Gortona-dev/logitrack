package com.gortona.logitrack.service;

import com.gortona.logitrack.entity.AppUser;
import com.gortona.logitrack.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

	public AppUser getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof AppUser user)) {
			throw new UnauthorizedException("Usuário não autenticado");
		}

		return user;
	}
}
