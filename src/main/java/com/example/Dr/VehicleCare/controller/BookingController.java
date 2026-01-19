package com.example.Dr.VehicleCare.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.dto.BookingRequest;
import com.example.Dr.VehicleCare.dto.CustomizedServiceDTO;
import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.CustomizedServiceRequest;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.BookingStatus;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.model.enums.ServiceType;
import com.example.Dr.VehicleCare.repository.*;

import com.example.Dr.VehicleCare.service.EmailService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BikeCompanyRepository bikeCompanyRepository;
    private final BikeModelRepository bikeModelRepository;
    private final CustomizedServiceRepository customizedServiceRepository;
    private final EmailService emailService;

    public BookingController(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BikeCompanyRepository bikeCompanyRepository,
            BikeModelRepository bikeModelRepository,
            CustomizedServiceRepository customizedServiceRepository,
            EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.bikeCompanyRepository = bikeCompanyRepository;
        this.bikeModelRepository = bikeModelRepository;
        this.customizedServiceRepository = customizedServiceRepository;
        this.emailService = emailService;
    }

    // ================= CREATE BOOKING =================
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest req, Principal principal) {

        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Long userId;
        try {
            userId = Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body("Invalid user ID");
        }

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBikeCompany(
                bikeCompanyRepository.findById(req.getBikeCompanyId()).orElse(null)
        );
        booking.setBikeModel(
                bikeModelRepository.findById(req.getBikeModelId()).orElse(null)
        );

        // Validate service type
        try {
            booking.setServiceType(ServiceType.valueOf(req.getServiceType()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid service type");
        }

        booking.setAppointmentDate(LocalDate.parse(req.getAppointmentDate()));
        booking.setFullAddress(req.getFullAddress());
        booking.setCity(req.getCity());
        booking.setPincode(req.getPincode());
        booking.setLandmark(req.getLandmark());
        booking.setTimeSlot(req.getTimeSlot());
        booking.setLatitude(req.getLatitude());
        booking.setLongitude(req.getLongitude());
        booking.setNotes(req.getNotes());
        booking.setStatus(BookingStatus.PENDING);

        // ===================== CUSTOMIZED SERVICE =====================
        if ("CUSTOMIZED".equalsIgnoreCase(req.getServiceType()) &&
                req.getCustomizedService() != null) {

            CustomizedServiceDTO dto = req.getCustomizedService();

            CustomizedServiceRequest csr = new CustomizedServiceRequest();
            csr.setUserId(userId);
            csr.setBikeCompany(dto.getBikeCompany());
            csr.setBikeModel(dto.getBikeModel());
            csr.setCc(dto.getCc());
            csr.setWash(dto.isWash());
            csr.setOilChange(dto.isOilChange());
            csr.setChainLube(dto.isChainLube());
            csr.setEngineTuneUp(dto.isEngineTuneUp());
            csr.setBreakCheck(dto.isBreakCheck());
            csr.setFullbodyPolishing(dto.isFullbodyPolishing());
            csr.setGeneralInspection(dto.isGeneralInspection());
            csr.setTotalPrice(dto.getTotalPrice());

            customizedServiceRepository.save(csr);
            booking.setCustomizedService(csr);
            booking.setServicePrice(dto.getTotalPrice());
        } else {
            booking.setServicePrice(req.getServicePrice());
        }

        booking.setPaymentStatus(PaymentStatus.UNPAID);

        Booking savedBooking = bookingRepository.save(booking);

        // Notify admin
        emailService.notifyAdminBooking(
                "New booking: " + user.getName() + " (" + user.getPhone() + ")"
        );

        return ResponseEntity.ok(savedBooking);
    }

    // ================= GET MY BOOKINGS =================
    @GetMapping("/my")
    public ResponseEntity<?> myBookings(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Long userId;
        try {
            userId = Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body("Invalid user ID");
        }

        return ResponseEntity.ok(bookingRepository.findByUserId(userId));
    }

    // ================= GET BOOKING BY ID =================
   @GetMapping("/{id}")
public ResponseEntity<?> getBooking(@PathVariable Long id) {
    if (id == null) {
        return ResponseEntity.badRequest().body("Booking ID required");
    }

    return bookingRepository.findById(id)
            .map(booking -> ResponseEntity.ok(booking))
            .orElseGet(() -> ResponseEntity.status(404).body("Booking not found"));
}


    // ================= DELETE BOOKING =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        if (id == null || !bookingRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Booking not found");
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }
}

