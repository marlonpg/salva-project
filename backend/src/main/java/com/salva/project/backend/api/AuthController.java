package com.salva.project.backend.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salva.project.backend.api.dto.AccessCodeVerifyPayload;
import com.salva.project.backend.api.dto.LoginRequestCreatePayload;
import com.salva.project.backend.api.dto.LoginRequestDTO;
import com.salva.project.backend.api.dto.SessionPayload;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/request")
	public LoginRequestDTO requestLoginAccess(@RequestBody LoginRequestCreatePayload payload) {
		return authService.requestLoginAccess(payload);
	}

	@PostMapping("/verify-code")
	public SessionPayload verifyCode(@RequestBody AccessCodeVerifyPayload payload) {
		return authService.verifyAccessCode(payload);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
		String token = extractTokenFromHeader(authHeader);
		authService.logout(token);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/validate")
	public ResponseEntity<ValidateResponse> validate(@RequestHeader("Authorization") String authHeader) {
		String token = extractTokenFromHeader(authHeader);
		String email = authService.validateSession(token);
		return ResponseEntity.ok(new ValidateResponse(true, email));
	}

	@PostMapping("/bootstrap")
	public ResponseEntity<String> bootstrap() {
		String code = authService.generateBootstrapCode();
		return ResponseEntity.ok(code);
	}

	private String extractTokenFromHeader(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Invalid authorization header");
		}
		return authHeader.substring(7);
	}

	public record ValidateResponse(boolean valid, String email) {
	}
}
