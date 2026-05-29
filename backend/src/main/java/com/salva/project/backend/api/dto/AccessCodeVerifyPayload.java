package com.salva.project.backend.api.dto;

public record AccessCodeVerifyPayload(
	String email,
	String code
) {
}
