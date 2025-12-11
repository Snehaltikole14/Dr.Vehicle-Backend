package com.example.Dr.VehicleCare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.model.BikeCompany;
import com.example.Dr.VehicleCare.model.BikeModel;
import com.example.Dr.VehicleCare.repository.BikeCompanyRepository;
import com.example.Dr.VehicleCare.repository.BikeModelRepository;

@RestController
@RequestMapping("/api/bikes")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BikeController {

    private final BikeCompanyRepository companyRepo;
    private final BikeModelRepository modelRepo;

    public BikeController(BikeCompanyRepository companyRepo, BikeModelRepository modelRepo) {
        this.companyRepo = companyRepo;
        this.modelRepo = modelRepo;
    }

    // Get all companies
    @GetMapping("/companies")
    public List<BikeCompany> getCompanies() {
        return companyRepo.findAll();
    }

    // Get all models for a company safely
    @GetMapping("/companies/{companyId}/models")
    public ResponseEntity<List<BikeModel>> getModelsByCompany(@PathVariable String companyId) {
        try {
            if (companyId == null || companyId.equals("undefined") || companyId.isBlank()) {
                return ResponseEntity.badRequest().body(List.of());
            }
            Long id = Long.valueOf(companyId);
            return ResponseEntity.ok(modelRepo.findByCompanyId(id));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }
}
