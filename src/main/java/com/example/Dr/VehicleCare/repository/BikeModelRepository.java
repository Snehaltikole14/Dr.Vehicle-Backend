
package com.example.Dr.VehicleCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.Dr.VehicleCare.model.BikeModel;

@Repository
public interface BikeModelRepository extends JpaRepository<BikeModel, Long> {
    // Fetch all models by company
    List<BikeModel> findByCompanyId(Long companyId);
}
