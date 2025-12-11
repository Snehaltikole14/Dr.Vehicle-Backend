package com.example.Dr.VehicleCare.controller;




import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.enums.BookingStatus;
import com.example.Dr.VehicleCare.repository.BookingRepository;

@RestController
@RequestMapping("/api/technician")
public class TechnicianController {
    private final BookingRepository bookingRepository;

    public TechnicianController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> myAssigned(Principal principal) {
        Long techId = Long.parseLong(principal.getName());
        // simple find; for performance add query method
        List<Booking> all = bookingRepository.findAll();
        var assigned = all.stream().filter(b -> b.getTechnician() != null && b.getTechnician().getId().equals(techId)).toList();
        return ResponseEntity.ok(assigned);
    }

    @PatchMapping("/bookings/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id, Principal principal) {
        Long techId = Long.parseLong(principal.getName());
        var bOpt = bookingRepository.findById(id);
        if(bOpt.isEmpty()) return ResponseEntity.notFound().build();
        Booking b = bOpt.get();
        if (b.getTechnician() == null || !b.getTechnician().getId().equals(techId)) {
            return ResponseEntity.status(403).body("Not authorized for this booking");
        }
        b.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(b);
        return ResponseEntity.ok(b);
    }
}
