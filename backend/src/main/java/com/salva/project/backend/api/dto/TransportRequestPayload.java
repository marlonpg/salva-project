package com.salva.project.backend.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.salva.project.backend.domain.Status;

public record TransportRequestPayload(
	Long id,
	Status status,
	String description,
	String requester,
	String requesterIdNumber,
	String requesterEmail,
	LocalDate serviceDate,
	Instant createdAt,
	Instant updatedAt,
	BigDecimal amount,
	BigDecimal tax,
	List<TransportRequestTeamPayload> team
) {
}
