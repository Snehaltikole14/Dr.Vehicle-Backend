package com.example.Dr.VehicleCare.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;



import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPaymentStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime start, LocalDateTime end);
    List<Booking> findByPaymentStatusAndCreatedAtAfter(PaymentStatus status, LocalDateTime from);
    List<Booking> findByPaymentStatus(PaymentStatus status);
    List<Booking> findByUserId(Long userId);

}
