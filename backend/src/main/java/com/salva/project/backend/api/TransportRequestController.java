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

import com.salva.project.backend.api.dto.TransportRequestPayload;

@RestController
@RequestMapping("/api/transport-requests")
public class TransportRequestController {

	private final TransportRequestService service;

	public TransportRequestController(TransportRequestService service) {
		this.service = service;
	}

	@GetMapping
	public List<TransportRequestPayload> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public TransportRequestPayload findById(@PathVariable Long id) {
		return service.findById(id);
	}

	@PostMapping
	public ResponseEntity<TransportRequestPayload> create(@RequestBody TransportRequestPayload payload) {
		TransportRequestPayload created = service.create(payload);
		return ResponseEntity
			.created(URI.create("/api/transport-requests/" + created.id()))
			.body(created);
	}

	@PutMapping("/{id}")
	public TransportRequestPayload update(@PathVariable Long id, @RequestBody TransportRequestPayload payload) {
		return service.update(id, payload);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
