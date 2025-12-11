package com.example.Dr.VehicleCare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.model.BikeCompany;
import com.example.Dr.VehicleCare.model.BikeModel;
import com.example.Dr.VehicleCare.repository.BikeCompanyRepository;
import com.example.Dr.VehicleCare.repository.BikeModelRepository;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminBikeController {

    private final BikeCompanyRepository companyRepo;
    private final BikeModelRepository modelRepo;

    public AdminBikeController(BikeCompanyRepository companyRepo, BikeModelRepository modelRepo) {
        this.companyRepo = companyRepo;
        this.modelRepo = modelRepo;
    }

    // ===================== ADD COMPANY =====================
    @PostMapping("/company")
    public ResponseEntity<?> addCompany(@RequestBody BikeCompany company) {
        if (company.getName() == null || company.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Company name is required");
        }
        return ResponseEntity.ok(companyRepo.save(company));
    }

    // ===================== ADD MODEL =====================
    @PostMapping("/model")
    public ResponseEntity<?> addModel(@RequestBody BikeModel model) {

        if (model.getCompany() == null || model.getCompany().getId() == null) {
            return ResponseEntity.badRequest().body("Company ID is required");
        }

        BikeCompany company = companyRepo.findById(model.getCompany().getId())
                .orElse(null);

        if (company == null) {
            return ResponseEntity.badRequest().body("Invalid company ID");
        }

        model.setCompany(company);

        return ResponseEntity.ok(modelRepo.save(model));
    }

    // ===================== GET ALL COMPANIES =====================
    @GetMapping("/companies")
    public List<BikeCompany> getCompanies() {
        return companyRepo.findAll();
    }

    // ===================== GET ALL MODELS =====================
    @GetMapping("/models")
    public List<BikeModel> getModels() {
        return modelRepo.findAll();
    }

    // ===================== GET MODELS BY COMPANY =====================
    @GetMapping("/models/{companyId}")
    public ResponseEntity<?> getModelsByCompany(@PathVariable Long companyId) {
        if (!companyRepo.existsById(companyId)) {
            return ResponseEntity.badRequest().body("Company not found");
        }
        return ResponseEntity.ok(modelRepo.findByCompanyId(companyId));
    }

    // ===================== DELETE COMPANY =====================
    @DeleteMapping("/company/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        if (!companyRepo.existsById(id)) {
            return ResponseEntity.badRequest().body("Company not found");
        }
        companyRepo.deleteById(id);
        return ResponseEntity.ok("Company deleted successfully");
    }

    // ===================== DELETE MODEL =====================
    @DeleteMapping("/model/{id}")
    public ResponseEntity<?> deleteModel(@PathVariable Long id) {
        if (!modelRepo.existsById(id)) {
            return ResponseEntity.badRequest().body("Model not found");
        }
        modelRepo.deleteById(id);
        return ResponseEntity.ok("Model deleted successfully");
    }
}
