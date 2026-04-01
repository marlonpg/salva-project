package com.salva.project.backend.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transport_request_team")
public class TransportRequestTeam {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "transport_request_id", nullable = false)
	private TransportRequest transportRequest;

	@Column(name = "person_name", nullable = false)
	private String personName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private Role role;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public TransportRequest getTransportRequest() {
		return transportRequest;
	}

	public void setTransportRequest(TransportRequest transportRequest) {
		this.transportRequest = transportRequest;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}
