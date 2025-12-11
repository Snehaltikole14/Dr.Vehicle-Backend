package com.example.Dr.VehicleCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.Bike;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    List<Bike> findByUserId(Long userId);
}