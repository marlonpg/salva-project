package com.salva.project.backend.api.dto;

public record AdminApprovePayload(
	Long loginRequestId,
	String adminEmail
) {
}
