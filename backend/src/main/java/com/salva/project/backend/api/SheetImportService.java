package com.salva.project.backend.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.salva.project.backend.api.dto.SheetImportResultPayload;
import com.salva.project.backend.domain.Role;
import com.salva.project.backend.domain.Status;
import com.salva.project.backend.domain.TransportRequest;
import com.salva.project.backend.domain.TransportRequestImported;
import com.salva.project.backend.domain.TransportRequestTeam;
import com.salva.project.backend.repository.TransportRequestImportedRepository;
import com.salva.project.backend.repository.TransportRequestRepository;

@Service
public class SheetImportService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private final TransportRequestRepository requestRepository;
	private final TransportRequestImportedRepository importedRepository;

	public SheetImportService(
		TransportRequestRepository requestRepository,
		TransportRequestImportedRepository importedRepository
	) {
		this.requestRepository = requestRepository;
		this.importedRepository = importedRepository;
	}

	@Transactional
	public SheetImportResultPayload importSheet(MultipartFile file) {
		int imported = 0;
		int skipped = 0;
		List<String> errors = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

			for (CSVRecord record : csvParser) {
				try {
					if (shouldSkipRow(record)) {
						skipped++;
						continue;
					}

					Integer importNumber = parseInteger(record.get("Nº"));
					if (importNumber == null) {
						errors.add("Row " + record.getRecordNumber() + ": Missing import number");
						continue;
					}

					if (importedRepository.existsByImportNumber(importNumber)) {
						skipped++;
						continue;
					}

					TransportRequest request = createTransportRequest(record);
					TransportRequest savedRequest = requestRepository.save(request);

					TransportRequestImported imported_record = createTransportRequestImported(record, importNumber, savedRequest);
					importedRepository.save(imported_record);

					imported++;
				} catch (Exception e) {
					errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
				}
			}
		} catch (Exception e) {
			errors.add("Failed to parse CSV: " + e.getMessage());
		}

		return new SheetImportResultPayload(imported, skipped, errors);
	}

	private boolean shouldSkipRow(CSVRecord record) {
		return record.size() == 0 || record.get(0).trim().isEmpty();
	}

	private TransportRequest createTransportRequest(CSVRecord record) {
		TransportRequest request = new TransportRequest();

		request.setStatus(parseStatus(record.get("CONCLUIDO?")));
		request.setDescription(record.get("Descrição"));
		request.setRequester(record.get(4));
		request.setRequesterIdNumber("");
		request.setRequesterEmail("");
		request.setServiceDate(parseDate(record.get(10)));
		request.setAmount(parseCurrency(record.get("Valor")));
		request.setTax(parseCurrency(record.get("Imposto")));

		TransportRequestTeam vetTeam = new TransportRequestTeam();
		vetTeam.setPersonName(record.get(4));
		vetTeam.setRole(Role.VETERINARIAN);
		vetTeam.setAmount(parseCurrency(record.get("Veterinário")));
		request.addTeamMember(vetTeam);

		String driverName = record.get(7);
		if (driverName != null && !driverName.trim().isEmpty()) {
			TransportRequestTeam driverTeam = new TransportRequestTeam();
			driverTeam.setPersonName(driverName);
			driverTeam.setRole(Role.DRIVER);
			driverTeam.setAmount(parseCurrency(record.get("Motorista")));
			request.addTeamMember(driverTeam);
		}

		return request;
	}

	private TransportRequestImported createTransportRequestImported(
		CSVRecord record,
		Integer importNumber,
		TransportRequest transportRequest
	) {
		TransportRequestImported imported = new TransportRequestImported();
		imported.setImportNumber(importNumber);
		imported.setTransportRequest(transportRequest);
		imported.setExtraAmount(parseCurrency(record.get("Extra")));
		imported.setResultAmount(parseCurrency(record.get("Resultado")));
		imported.setMonth(record.get("Mês"));
		imported.setServiceInvoice(nullIfEmpty(record.get("Nota Fiscal Serviço")));
		imported.setVetInvoice(nullIfEmpty(record.get("Nota Fiscal Vet")));
		return imported;
	}

	private Status parseStatus(String status) {
		if (status == null || status.trim().isEmpty() || "OK".equalsIgnoreCase(status.trim())) {
			return Status.DONE;
		}
		return Status.DONE;
	}

	private BigDecimal parseCurrency(String value) {
		if (value == null || value.trim().isEmpty()) {
			return BigDecimal.ZERO;
		}
		String cleaned = value.trim()
			.replaceAll("^R\\$\\s*", "")
			.replace(",", "");
		try {
			return new BigDecimal(cleaned);
		} catch (NumberFormatException e) {
			return BigDecimal.ZERO;
		}
	}

	private LocalDate parseDate(String date) {
		if (date == null || date.trim().isEmpty()) {
			return LocalDate.now();
		}
		try {
			return LocalDate.parse(date.trim(), DATE_FORMATTER);
		} catch (Exception e) {
			return LocalDate.now();
		}
	}

	private Integer parseInteger(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String nullIfEmpty(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}
}
