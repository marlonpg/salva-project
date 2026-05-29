package com.salva.project.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.SessionToken;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
	Optional<SessionToken> findByJwtToken(String jwtToken);
}
