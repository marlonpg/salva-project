package com.salva.project.backend.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salva.project.backend.api.dto.AccessCodeVerifyPayload;
import com.salva.project.backend.api.dto.LoginRequestCreatePayload;
import com.salva.project.backend.api.dto.LoginRequestDTO;
import com.salva.project.backend.api.dto.SessionPayload;
import com.salva.project.backend.domain.AccessCode;
import com.salva.project.backend.domain.LoginRequest;
import com.salva.project.backend.domain.LoginRequestStatus;
import com.salva.project.backend.domain.SessionToken;
import com.salva.project.backend.repository.AccessCodeRepository;
import com.salva.project.backend.repository.LoginRequestRepository;
import com.salva.project.backend.repository.SessionTokenRepository;

@Service
public class AuthService {

	private final LoginRequestRepository loginRequestRepo;
	private final AccessCodeRepository accessCodeRepo;
	private final SessionTokenRepository sessionTokenRepo;
	private final JwtTokenProvider jwtProvider;
	private final EmailService emailService;

	@Value("${app.admin.emails:}")
	private String adminEmailsConfig;

	public AuthService(
		LoginRequestRepository loginRequestRepo,
		AccessCodeRepository accessCodeRepo,
		SessionTokenRepository sessionTokenRepo,
		JwtTokenProvider jwtProvider,
		EmailService emailService
	) {
		this.loginRequestRepo = loginRequestRepo;
		this.accessCodeRepo = accessCodeRepo;
		this.sessionTokenRepo = sessionTokenRepo;
		this.jwtProvider = jwtProvider;
		this.emailService = emailService;
	}

	@Transactional
	public LoginRequestDTO requestLoginAccess(LoginRequestCreatePayload payload) {
		String email = payload.email().trim().toLowerCase();

		loginRequestRepo.findByEmail(email)
			.ifPresent(existing -> {
				if (existing.getStatus() == LoginRequestStatus.PENDING) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login request already pending");
				}
			});

		LoginRequest request = new LoginRequest(email);
		request = loginRequestRepo.save(request);

		return toDTO(request);
	}

	@Transactional
	public void approveLoginRequest(Long requestId, String adminEmail) {
		if (!isAdmin(adminEmail)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can approve login requests");
		}

		LoginRequest request = loginRequestRepo.findById(requestId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Login request not found"));

		if (request.getStatus() != LoginRequestStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be approved");
		}

		String code = generateAccessCode();
		Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

		AccessCode accessCode = new AccessCode(request.getId(), code, expiresAt);
		accessCodeRepo.save(accessCode);

		request.setStatus(LoginRequestStatus.APPROVED);
		request.setApprovedAt(Instant.now());
		request.setApprovedBy(adminEmail);
		loginRequestRepo.save(request);

		emailService.sendAccessCode(request.getEmail(), code);
	}

	@Transactional
	public void rejectLoginRequest(Long requestId) {
		LoginRequest request = loginRequestRepo.findById(requestId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Login request not found"));

		if (request.getStatus() != LoginRequestStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be rejected");
		}

		request.setStatus(LoginRequestStatus.REJECTED);
		loginRequestRepo.save(request);
	}

	@Transactional
	public SessionPayload verifyAccessCode(AccessCodeVerifyPayload payload) {
		String email = payload.email().trim().toLowerCase();
		String code = payload.code().trim();

		AccessCode accessCode = accessCodeRepo.findByCode(code)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));

		if (accessCode.getExpiresAt().isBefore(Instant.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code has expired");
		}

		if (accessCode.getUsedAt() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code has already been used");
		}

		LoginRequest loginRequest = loginRequestRepo.findById(accessCode.getLoginRequestId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Login request not found"));

		if (!loginRequest.getEmail().equals(email)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email does not match");
		}

		Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
		String jwtToken = jwtProvider.generateToken(email, expiresAt);

		SessionToken sessionToken = new SessionToken(email, jwtToken, expiresAt);
		sessionTokenRepo.save(sessionToken);

		accessCode.setUsedAt(Instant.now());
		accessCodeRepo.save(accessCode);

		Long expiresIn = ChronoUnit.SECONDS.between(Instant.now(), expiresAt);

		return new SessionPayload(jwtToken, expiresIn, email);
	}

	@Transactional(readOnly = true)
	public String validateSession(String jwtToken) {
		if (!jwtProvider.isTokenValid(jwtToken)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
		}

		sessionTokenRepo.findByJwtToken(jwtToken)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session not found"));

		String email = jwtProvider.extractEmail(jwtToken);
		if (email == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not contain email");
		}

		return email;
	}

	@Transactional
	public void logout(String jwtToken) {
		sessionTokenRepo.findByJwtToken(jwtToken).ifPresent(sessionTokenRepo::delete);
	}

	@Transactional
	public String generateBootstrapCode() {
		String adminEmail = "gambadeveloper@gmail.com";
		LoginRequest request = loginRequestRepo.findByEmail(adminEmail)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

		String code = generateAccessCode();
		Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

		AccessCode accessCode = new AccessCode(request.getId(), code, expiresAt);
		accessCodeRepo.save(accessCode);

		emailService.sendAccessCode(request.getEmail(), code);
		return code;
	}

	private String generateAccessCode() {
		Random random = new Random();
		int code = 100000 + random.nextInt(900000);
		return String.valueOf(code);
	}

	private boolean isAdmin(String email) {
		if (adminEmailsConfig == null || adminEmailsConfig.trim().isEmpty()) {
			return false;
		}
		String[] admins = adminEmailsConfig.split(",");
		for (String admin : admins) {
			if (admin.trim().equalsIgnoreCase(email)) {
				return true;
			}
		}
		return false;
	}

	private LoginRequestDTO toDTO(LoginRequest request) {
		return new LoginRequestDTO(
			request.getId(),
			request.getEmail(),
			request.getStatus().toString(),
			request.getRequestedAt(),
			request.getApprovedAt(),
			request.getApprovedBy()
		);
	}
}
