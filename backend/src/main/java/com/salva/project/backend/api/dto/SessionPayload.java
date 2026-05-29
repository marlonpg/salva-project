package com.salva.project.backend.api.dto;

public record SessionPayload(
	String token,
	Long expiresIn,
	String email
) {
}
