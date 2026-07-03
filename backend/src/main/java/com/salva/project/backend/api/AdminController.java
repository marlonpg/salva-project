package com.salva.project.backend.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salva.project.backend.api.dto.LoginRequestDTO;
import com.salva.project.backend.domain.LoginRequestStatus;
import com.salva.project.backend.repository.AccessCodeRepository;
import com.salva.project.backend.repository.LoginRequestRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AuthService authService;
	private final LoginRequestRepository loginRequestRepo;
	private final AccessCodeRepository accessCodeRepo;

	public AdminController(AuthService authService, LoginRequestRepository loginRequestRepo, AccessCodeRepository accessCodeRepo) {
		this.authService = authService;
		this.loginRequestRepo = loginRequestRepo;
		this.accessCodeRepo = accessCodeRepo;
	}

	@GetMapping("/login-requests")
	public ResponseEntity<List<LoginRequestDTO>> getPendingRequests(
		@RequestHeader("Authorization") String authHeader
	) {
		String adminEmail = extractEmailFromAuth(authHeader);

		List<LoginRequestDTO> requests = loginRequestRepo.findByStatus(LoginRequestStatus.PENDING)
			.stream()
			.map(this::toDTO)
			.toList();

		return ResponseEntity.ok(requests);
	}

	@GetMapping("/login-requests/history")
	public ResponseEntity<List<LoginRequestDTO>> getRequestHistory(
		@RequestHeader("Authorization") String authHeader
	) {
		String adminEmail = extractEmailFromAuth(authHeader);

		List<LoginRequestDTO> requests = loginRequestRepo.findAll()
			.stream()
			.map(this::toDTO)
			.toList();

		return ResponseEntity.ok(requests);
	}

	@PostMapping("/login-requests/{id}/approve")
	public ResponseEntity<Void> approveRequest(
		@PathVariable Long id,
		@RequestHeader("Authorization") String authHeader
	) {
		String adminEmail = extractEmailFromAuth(authHeader);
		authService.approveLoginRequest(id, adminEmail);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/login-requests/{id}/reject")
	public ResponseEntity<Void> rejectRequest(
		@PathVariable Long id,
		@RequestHeader("Authorization") String authHeader
	) {
		String adminEmail = extractEmailFromAuth(authHeader);
		authService.rejectLoginRequest(id);
		return ResponseEntity.noContent().build();
	}

	private String extractEmailFromAuth(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Invalid authorization header");
		}
		String token = authHeader.substring(7);
		return authService.validateSession(token);
	}

	private LoginRequestDTO toDTO(com.salva.project.backend.domain.LoginRequest request) {
		String code = accessCodeRepo.findByLoginRequestId(request.getId())
			.map(ac -> ac.getCode())
			.orElse(null);

		return new LoginRequestDTO(
			request.getId(),
			request.getEmail(),
			request.getStatus().toString(),
			request.getRequestedAt(),
			request.getApprovedAt(),
			request.getApprovedBy(),
			code
		);
	}
}
