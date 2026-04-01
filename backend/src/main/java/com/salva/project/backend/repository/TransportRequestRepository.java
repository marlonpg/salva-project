package com.salva.project.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.TransportRequest;

public interface TransportRequestRepository extends JpaRepository<TransportRequest, Long> {
	@Override
	@EntityGraph(attributePaths = "team")
	List<TransportRequest> findAll();

	@Override
	@EntityGraph(attributePaths = "team")
	Optional<TransportRequest> findById(Long id);
}
