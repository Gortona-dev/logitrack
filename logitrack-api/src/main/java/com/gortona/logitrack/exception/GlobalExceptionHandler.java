package com.gortona.logitrack.exception;

import com.gortona.logitrack.dto.common.ApiErrorResponse;
import com.gortona.logitrack.dto.common.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflictException(
			ConflictException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.CONFLICT;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				exception.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
			ResourceNotFoundException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				exception.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(
			BusinessRuleException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				exception.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(
			UnauthorizedException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				exception.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(fieldError -> new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
				.toList();

		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				"Falha na validação da requisição",
				request.getRequestURI(),
				fieldErrors
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				"Parâmetro inválido na requisição: " + exception.getName(),
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
		log.error("Erro inesperado ao processar {} {}", request.getMethod(), request.getRequestURI(), exception);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				errorTitle(status),
				"Erro interno inesperado",
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}

	private String errorTitle(HttpStatus status) {
		return switch (status) {
			case BAD_REQUEST -> "Requisição inválida";
			case UNAUTHORIZED -> "Não autorizado";
			case CONFLICT -> "Conflito";
			case NOT_FOUND -> "Não encontrado";
			case INTERNAL_SERVER_ERROR -> "Erro interno";
			default -> "Erro";
		};
	}
}
