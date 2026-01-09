package com.example.Dr.VehicleCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.Payment;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Payment findByRazorpayOrderId(String razorpayOrderId);
    List<Payment> findByStatus(PaymentStatus status);
}


