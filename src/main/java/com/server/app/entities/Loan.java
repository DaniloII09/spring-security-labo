package com.server.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.server.app.entities.enums.LoanStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loans")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requested_principal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal requestedPrincipalAmount;

    @Column(name = "annual_interest_rate", precision = 6, scale = 2, nullable = false)
    private BigDecimal annualInterestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}