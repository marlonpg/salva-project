package com.salva.project.backend.api.dto;

import java.util.List;

public record SheetImportResultPayload(
	int imported,
	int skipped,
	List<String> errors
) {
}
