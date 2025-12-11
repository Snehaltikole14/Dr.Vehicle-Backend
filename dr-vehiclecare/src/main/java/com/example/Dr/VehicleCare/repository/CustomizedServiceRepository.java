package com.example.Dr.VehicleCare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Dr.VehicleCare.model.CustomizedServiceRequest;

public interface CustomizedServiceRepository extends JpaRepository<CustomizedServiceRequest, Long> {
    List<CustomizedServiceRequest> findByUserId(Long userId);
}
