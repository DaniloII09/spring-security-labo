package com.server.app.repositories;

import com.server.app.entities.PaymentPlan;
import com.server.app.entities.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    List<PaymentPlan> findByLoan_IdOrderByInstallmentNumberAsc(Long loanId);
    List<PaymentPlan> findByLoan_IdAndStatusOrderByInstallmentNumberAsc(Long loanId, InstallmentStatus status);
    List<PaymentPlan> findByLoan_User_Id(int userId);
    List<PaymentPlan> findByLoan_User_IdAndStatus(int userId, InstallmentStatus status);
    long countByLoan_IdAndStatus(Long loanId, InstallmentStatus status);
}
