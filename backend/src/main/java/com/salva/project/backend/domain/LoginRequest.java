package com.salva.project.backend.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "login_request", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class LoginRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LoginRequestStatus status;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "approved_at")
	private Instant approvedAt;

	@Column(name = "approved_by")
	private String approvedBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public LoginRequest() {
	}

	public LoginRequest(String email) {
		this.email = email;
		this.status = LoginRequestStatus.PENDING;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		requestedAt = now;
		if (status == null) {
			status = LoginRequestStatus.PENDING;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LoginRequestStatus getStatus() {
		return status;
	}

	public void setStatus(LoginRequestStatus status) {
		this.status = status;
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(Instant requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Instant approvedAt) {
		this.approvedAt = approvedAt;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
