package com.salva.project.backend.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.salva.project.backend.api.dto.SheetImportResultPayload;

@RestController
@RequestMapping("/api/import")
public class SheetImportController {

	private final SheetImportService service;

	public SheetImportController(SheetImportService service) {
		this.service = service;
	}

	@PostMapping("/sheet")
	public ResponseEntity<SheetImportResultPayload> importSheet(@RequestParam("file") MultipartFile file) {
		SheetImportResultPayload result = service.importSheet(file);
		return ResponseEntity.ok(result);
	}
}
