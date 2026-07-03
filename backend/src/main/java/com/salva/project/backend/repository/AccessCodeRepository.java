package com.salva.project.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salva.project.backend.domain.AccessCode;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {
	Optional<AccessCode> findByCode(String code);

	@Query(value = "SELECT * FROM access_code WHERE login_request_id = :loginRequestId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
	Optional<AccessCode> findLatestByLoginRequestId(@Param("loginRequestId") Long loginRequestId);
}
