package com.example.Dr.VehicleCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
