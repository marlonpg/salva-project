package com.salva.project.backend.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.salva.project.backend.domain.ExpenseCategory;
import com.salva.project.backend.domain.ExpenseType;

public record ExpensePayload(
	Long id,
	String referencia,
	String requerente,
	ExpenseCategory categoria,
	ExpenseType tipo,
	Boolean pago,
	BigDecimal valor,
	LocalDate dataLancamento,
	Instant createdAt,
	Instant updatedAt,
	Integer occurrences
) {
}
