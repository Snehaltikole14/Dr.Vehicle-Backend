package com.example.Dr.VehicleCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.Payment;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByRazorpayOrderId(String razorpayOrderId);
    List<Payment> findByStatus(PaymentStatus status);
}
