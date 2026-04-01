package com.salva.project.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.TransportRequestTeam;

public interface TransportRequestTeamRepository extends JpaRepository<TransportRequestTeam, UUID> {
}
