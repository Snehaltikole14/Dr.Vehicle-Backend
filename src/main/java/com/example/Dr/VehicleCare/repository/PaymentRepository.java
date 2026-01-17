package com.example.Dr.VehicleCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.Payment;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByStatus(PaymentStatus status);
}
