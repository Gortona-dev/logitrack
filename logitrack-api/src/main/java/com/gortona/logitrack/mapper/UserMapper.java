package com.gortona.logitrack.mapper;

import com.gortona.logitrack.dto.user.UserResponse;
import com.gortona.logitrack.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(AppUser user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.isActive(),
				user.getDocument(),
				user.getPhone(),
				user.getClientId(),
				user.getDeliveryPersonId(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
