
package com.example.Dr.VehicleCare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.Dr.VehicleCare.model.CustomizedServiceRequest;
import com.example.Dr.VehicleCare.repository.CustomizedServiceRepository;
import com.example.Dr.VehicleCare.service.CustomizedServiceService;

@RestController
@RequestMapping("/api/customized")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CustomizedServiceController {

    private final CustomizedServiceRepository repo;
    private final CustomizedServiceService service;

    public CustomizedServiceController(CustomizedServiceRepository repo,
                                       CustomizedServiceService service) {
        this.repo = repo;
        this.service = service;
    }

    @PostMapping("/calculate")
    public double calculatePrice(@RequestBody CustomizedServiceRequest req) {
        return service.calculatePrice(
                req.getCc(),
                req.isWash(),
                req.isOilChange(),
                req.isChainLube(),
                req.isEngineTuneUp(),
                req.isBreakCheck(),
                req.isFullbodyPolishing(),
                req.isGeneralInspection()
        );
    }

    @PostMapping("/save")
    public CustomizedServiceRequest saveCustomizedService(@RequestBody CustomizedServiceRequest req) {
        double price = service.calculatePrice(
                req.getCc(),
                req.isWash(),
                req.isOilChange(),
                req.isChainLube(),
                req.isEngineTuneUp(),
                req.isBreakCheck(),
                req.isFullbodyPolishing(),
                req.isGeneralInspection()
        );
        req.setTotalPrice(price);
        return repo.save(req);
    }

    @GetMapping("/all")
    public List<CustomizedServiceRequest> getAllServices() {
        return repo.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<CustomizedServiceRequest> getServicesByUser(@PathVariable Long userId) {
        return repo.findByUserId(userId);
    }

    // ====================
    // 🔹 DELETE SERVICE
    // ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ====================
    // 🔹 EDIT / UPDATE SERVICE
    // ====================
    @PutMapping("/{id}")
    public ResponseEntity<CustomizedServiceRequest> updateService(
            @PathVariable Long id,
            @RequestBody CustomizedServiceRequest updatedReq) {

        return repo.findById(id).map(existing -> {
            existing.setBikeCompany(updatedReq.getBikeCompany());
            existing.setBikeModel(updatedReq.getBikeModel());
            existing.setCc(updatedReq.getCc());
            existing.setWash(updatedReq.isWash());
            existing.setOilChange(updatedReq.isOilChange());
            existing.setChainLube(updatedReq.isChainLube());
            existing.setEngineTuneUp(updatedReq.isEngineTuneUp());
            existing.setBreakCheck(updatedReq.isBreakCheck());
            existing.setFullbodyPolishing(updatedReq.isFullbodyPolishing());
            existing.setGeneralInspection(updatedReq.isGeneralInspection());

            // Recalculate price
            double price = service.calculatePrice(
                    existing.getCc(),
                    existing.isWash(),
                    existing.isOilChange(),
                    existing.isChainLube(),
                    existing.isEngineTuneUp(),
                    existing.isBreakCheck(),
                    existing.isFullbodyPolishing(),
                    existing.isGeneralInspection()
            );
            existing.setTotalPrice(price);

            CustomizedServiceRequest saved = repo.save(existing);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    

}