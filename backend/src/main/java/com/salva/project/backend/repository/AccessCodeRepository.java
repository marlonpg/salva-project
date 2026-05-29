package com.salva.project.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.AccessCode;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {
	Optional<AccessCode> findByCode(String code);
}
