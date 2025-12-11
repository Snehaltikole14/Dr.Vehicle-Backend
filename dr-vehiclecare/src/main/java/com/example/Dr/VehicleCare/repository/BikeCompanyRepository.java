package com.example.Dr.VehicleCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.Dr.VehicleCare.model.BikeCompany;

@Repository
public interface BikeCompanyRepository extends JpaRepository<BikeCompany, Long> {
    // Optional: Add custom queries if needed
}

