package com.salva.project.backend.api.dto;

import java.time.Instant;

public record LoginRequestDTO(
	Long id,
	String email,
	String status,
	Instant requestedAt,
	Instant approvedAt,
	String approvedBy,
	String code
) {
}
