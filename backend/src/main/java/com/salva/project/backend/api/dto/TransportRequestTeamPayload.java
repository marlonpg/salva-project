package com.salva.project.backend.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.salva.project.backend.domain.Role;

public record TransportRequestTeamPayload(
	UUID id,
	String personName,
	Role role,
	BigDecimal amount
) {
}
