package com.salva.project.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.TransportRequest;

public interface TransportRequestRepository extends JpaRepository<TransportRequest, Long> {
}
