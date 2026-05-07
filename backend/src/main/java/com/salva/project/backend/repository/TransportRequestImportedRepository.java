package com.salva.project.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.TransportRequestImported;

public interface TransportRequestImportedRepository extends JpaRepository<TransportRequestImported, Long> {
	boolean existsByImportNumber(Integer importNumber);
}
