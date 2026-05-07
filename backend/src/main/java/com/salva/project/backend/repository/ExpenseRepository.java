package com.salva.project.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salva.project.backend.domain.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
