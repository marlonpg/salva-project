package com.salva.project.backend.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salva.project.backend.api.dto.TransportRequestPayload;
import com.salva.project.backend.api.dto.TransportRequestTeamPayload;
import com.salva.project.backend.domain.TransportRequest;
import com.salva.project.backend.domain.TransportRequestTeam;
import com.salva.project.backend.repository.TransportRequestRepository;

@Service
public class TransportRequestService {

	private final TransportRequestRepository repository;

	public TransportRequestService(TransportRequestRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<TransportRequestPayload> findAll() {
		return repository.findAll().stream()
			.map(this::toPayload)
			.toList();
	}

	@Transactional(readOnly = true)
	public TransportRequestPayload findById(Long id) {
		TransportRequest entity = repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transport request not found: " + id));
		return toPayload(entity);
	}

	@Transactional
	public TransportRequestPayload create(TransportRequestPayload payload) {
		TransportRequest entity = new TransportRequest();
		applyPayload(entity, payload);
		return toPayload(repository.save(entity));
	}

	@Transactional
	public TransportRequestPayload update(Long id, TransportRequestPayload payload) {
		TransportRequest entity = repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transport request not found: " + id));
		applyPayload(entity, payload);
		return toPayload(repository.save(entity));
	}

	@Transactional
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transport request not found: " + id);
		}
		repository.deleteById(id);
	}

	private void applyPayload(TransportRequest entity, TransportRequestPayload payload) {
		entity.setStatus(payload.status());
		entity.setDescription(payload.description());
		entity.setRequester(payload.requester());
		entity.setServiceDate(payload.serviceDate());
		entity.setAmount(payload.amount());
		entity.setTax(payload.tax());
		if (payload.createdAt() != null) {
			entity.setCreatedAt(payload.createdAt());
		}
		if (payload.updatedAt() != null) {
			entity.setUpdatedAt(payload.updatedAt());
		}
		entity.setTeam(toTeamEntities(payload.team()));
	}

	private List<TransportRequestTeam> toTeamEntities(List<TransportRequestTeamPayload> teamPayload) {
		if (teamPayload == null) {
			return List.of();
		}
		return teamPayload.stream()
			.map(member -> {
				TransportRequestTeam entity = new TransportRequestTeam();
				entity.setId(member.id());
				entity.setPersonName(member.personName());
				entity.setRole(member.role());
				entity.setAmount(member.amount());
				return entity;
			})
			.toList();
	}

	private TransportRequestPayload toPayload(TransportRequest entity) {
		List<TransportRequestTeamPayload> teamPayload = entity.getTeam().stream()
			.map(member -> new TransportRequestTeamPayload(
				member.getId(),
				member.getPersonName(),
				member.getRole(),
				member.getAmount()
			))
			.toList();

		return new TransportRequestPayload(
			entity.getId(),
			entity.getStatus(),
			entity.getDescription(),
			entity.getRequester(),
			entity.getServiceDate(),
			entity.getCreatedAt(),
			entity.getUpdatedAt(),
			entity.getAmount(),
			entity.getTax(),
			teamPayload
		);
	}
}
