package com.example.Dr.VehicleCare.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.BookingStatus;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public AdminController(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

  

   
    /** Assign technician to a booking */
    @PatchMapping("/bookings/{bookingId}/assign/{techId}")
    public ResponseEntity<?> assignTechnician(@PathVariable Long bookingId, @PathVariable Long techId) {

        var bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        var techOpt = userRepository.findById(techId);
        if (techOpt.isEmpty()) return ResponseEntity.status(404).body("Technician not found");

        User technician = techOpt.get();

        // Validate role
        if (technician.getRole() != UserRole.TECHNICIAN) {
            return ResponseEntity.badRequest().body("User is not a technician");
        }

        Booking booking = bookingOpt.get();
        booking.setTechnician(technician);
        booking.setStatus(BookingStatus.
        		COMPLETED);
        bookingRepository.save(booking);

        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {

        long totalUsers = userRepository.count();
        long totalMechanics = userRepository.countByRole(UserRole.TECHNICIAN);
        long totalBookings = bookingRepository.count();

        // Only PAID bookings
        List<Booking> paidBookings =
                bookingRepository.findByPaymentStatus(PaymentStatus.PAID);

        // Current month range
        LocalDateTime startOfMonth =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // ✅ MONTHLY REVENUE
        double monthlyRevenue = paidBookings.stream()
                .filter(b -> b.getCreatedAt() != null)
                .filter(b -> !b.getCreatedAt().isBefore(startOfMonth)
                          && !b.getCreatedAt().isAfter(now))
                .mapToDouble(b -> b.getServicePrice() != null ? b.getServicePrice() : 0)
                .sum();

        // ✅ REVENUE BY DAY (for graph)
        List<Map<String, Object>> revenueByMonth = paidBookings.stream()
                .filter(b -> b.getCreatedAt() != null)
                .filter(b -> !b.getCreatedAt().isBefore(startOfMonth))
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedAt().getDayOfMonth(),
                        Collectors.summingDouble(
                                b -> b.getServicePrice() != null ? b.getServicePrice() : 0
                        )
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> Map.<String, Object>of(
                        "month", "Day " + e.getKey(),
                        "amount", e.getValue()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalMechanics", totalMechanics,
                "totalBookings", totalBookings,
                "revenue", monthlyRevenue,
                "revenueByMonth", revenueByMonth
        ));
    }






    
    /** APPROVE booking */
    @PatchMapping("/bookings/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long id) {
        var bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        return ResponseEntity.ok("Booking approved");
    }

    /** REJECT booking */
    @PatchMapping("/bookings/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id) {
        var bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return ResponseEntity.ok("Booking rejected");
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }



    /** Get all users */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        var bookingOpt = bookingRepository.findById(id);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        return ResponseEntity.ok(bookingOpt.get());
    }
    
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        List<User> customers = userRepository.findByRole(UserRole.CUSTOMER);
        return ResponseEntity.ok(customers);
    }

    
    
    
    

}
