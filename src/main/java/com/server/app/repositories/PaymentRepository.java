package com.server.app.repositories;

import com.server.app.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPaymentPlan_Loan_User_Id(int userId);
}
