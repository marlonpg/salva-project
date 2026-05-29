package com.salva.project.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.LoginRequest;
import com.salva.project.backend.domain.LoginRequestStatus;

public interface LoginRequestRepository extends JpaRepository<LoginRequest, Long> {
	Optional<LoginRequest> findByEmail(String email);
	List<LoginRequest> findByStatus(LoginRequestStatus status);
}
