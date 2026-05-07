package com.salva.project.backend.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transport_request_imported")
public class TransportRequestImported {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "import_number", nullable = false, unique = true)
	private Integer importNumber;

	@ManyToOne(optional = false)
	@JoinColumn(name = "transport_request_id", nullable = false)
	private TransportRequest transportRequest;

	@Column(name = "extra_amount", precision = 15, scale = 2)
	private BigDecimal extraAmount;

	@Column(name = "result_amount", precision = 15, scale = 2)
	private BigDecimal resultAmount;

	@Column(name = "import_month", length = 50)
	private String month;

	@Column(name = "service_invoice", length = 255)
	private String serviceInvoice;

	@Column(name = "vet_invoice", length = 255)
	private String vetInvoice;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getImportNumber() {
		return importNumber;
	}

	public void setImportNumber(Integer importNumber) {
		this.importNumber = importNumber;
	}

	public TransportRequest getTransportRequest() {
		return transportRequest;
	}

	public void setTransportRequest(TransportRequest transportRequest) {
		this.transportRequest = transportRequest;
	}

	public BigDecimal getExtraAmount() {
		return extraAmount;
	}

	public void setExtraAmount(BigDecimal extraAmount) {
		this.extraAmount = extraAmount;
	}

	public BigDecimal getResultAmount() {
		return resultAmount;
	}

	public void setResultAmount(BigDecimal resultAmount) {
		this.resultAmount = resultAmount;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getServiceInvoice() {
		return serviceInvoice;
	}

	public void setServiceInvoice(String serviceInvoice) {
		this.serviceInvoice = serviceInvoice;
	}

	public String getVetInvoice() {
		return vetInvoice;
	}

	public void setVetInvoice(String vetInvoice) {
		this.vetInvoice = vetInvoice;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
