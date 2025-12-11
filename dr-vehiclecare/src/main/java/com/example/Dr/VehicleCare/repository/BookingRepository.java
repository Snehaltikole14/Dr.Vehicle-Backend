package com.example.Dr.VehicleCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatus(String status);
}
