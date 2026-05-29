package com.salva.project.backend.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "access_code")
public class AccessCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "login_request_id", nullable = false)
	private Long loginRequestId;

	@Column(nullable = false)
	private String code;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "used_at")
	private Instant usedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public AccessCode() {
	}

	public AccessCode(Long loginRequestId, String code, Instant expiresAt) {
		this.loginRequestId = loginRequestId;
		this.code = code;
		this.expiresAt = expiresAt;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLoginRequestId() {
		return loginRequestId;
	}

	public void setLoginRequestId(Long loginRequestId) {
		this.loginRequestId = loginRequestId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public Instant getUsedAt() {
		return usedAt;
	}

	public void setUsedAt(Instant usedAt) {
		this.usedAt = usedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
