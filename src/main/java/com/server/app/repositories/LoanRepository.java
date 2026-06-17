package com.server.app.repositories;

import com.server.app.entities.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Page<Loan> findByUser_Id(int userId, Pageable pageable);
    List<Loan> findByUser_Id(int userId);
    Optional<Loan> findByIdAndUser_Id(Long id, int userId);
}
