package com.salva.project.backend.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salva.project.backend.api.dto.ExpensePayload;
import com.salva.project.backend.domain.Expense;
import com.salva.project.backend.domain.ExpenseType;
import com.salva.project.backend.repository.ExpenseRepository;

@Service
public class ExpenseService {

	private final ExpenseRepository repository;

	public ExpenseService(ExpenseRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<ExpensePayload> findAll() {
		return repository.findAll().stream()
			.map(this::toPayload)
			.toList();
	}

	@Transactional(readOnly = true)
	public ExpensePayload findById(Long id) {
		Expense entity = repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + id));
		return toPayload(entity);
	}

	@Transactional
	public List<ExpensePayload> create(ExpensePayload payload) {
		int occurrences = payload.occurrences() != null ? payload.occurrences() : 1;
		List<Expense> created = new ArrayList<>();

		if (occurrences <= 1) {
			Expense entity = new Expense();
			applyPayload(entity, payload);
			created.add(repository.save(entity));
		} else {
			int daysToAdd = payload.tipo() == ExpenseType.MENSAL ? 30 : 365;

			for (int i = 0; i < occurrences; i++) {
				Expense entity = new Expense();
				applyPayload(entity, payload);

				LocalDate newDate = payload.dataLancamento().plusDays((long) i * daysToAdd);
				entity.setDataLancamento(newDate);

				String suffix = "/" + (i + 1);
				entity.setReferencia(payload.referencia() + suffix);

				created.add(repository.save(entity));
			}
		}

		return created.stream()
			.map(this::toPayload)
			.toList();
	}

	@Transactional
	public ExpensePayload update(Long id, ExpensePayload payload) {
		Expense entity = repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + id));
		applyPayload(entity, payload);
		return toPayload(repository.save(entity));
	}

	@Transactional
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + id);
		}
		repository.deleteById(id);
	}

	private void applyPayload(Expense entity, ExpensePayload payload) {
		entity.setReferencia(payload.referencia());
		entity.setRequerente(payload.requerente());
		entity.setCategoria(payload.categoria());
		entity.setTipo(payload.tipo());
		entity.setPago(payload.pago() != null ? payload.pago() : false);
		entity.setValor(payload.valor());
		entity.setDataLancamento(payload.dataLancamento());
		if (payload.createdAt() != null) {
			entity.setCreatedAt(payload.createdAt());
		}
		if (payload.updatedAt() != null) {
			entity.setUpdatedAt(payload.updatedAt());
		}
	}

	private ExpensePayload toPayload(Expense entity) {
		return new ExpensePayload(
			entity.getId(),
			entity.getReferencia(),
			entity.getRequerente(),
			entity.getCategoria(),
			entity.getTipo(),
			entity.getPago(),
			entity.getValor(),
			entity.getDataLancamento(),
			entity.getCreatedAt(),
			entity.getUpdatedAt(),
			null
		);
	}
}
