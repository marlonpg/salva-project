package com.salva.project.backend.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salva.project.backend.api.dto.ExpensePayload;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

	private final ExpenseService service;

	public ExpenseController(ExpenseService service) {
		this.service = service;
	}

	@GetMapping
	public List<ExpensePayload> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public ExpensePayload findById(@PathVariable Long id) {
		return service.findById(id);
	}

	@PostMapping
	public ResponseEntity<List<ExpensePayload>> create(@RequestBody ExpensePayload payload) {
		List<ExpensePayload> created = service.create(payload);
		return ResponseEntity
			.created(URI.create("/api/expenses"))
			.body(created);
	}

	@PutMapping("/{id}")
	public ExpensePayload update(@PathVariable Long id, @RequestBody ExpensePayload payload) {
		return service.update(id, payload);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
