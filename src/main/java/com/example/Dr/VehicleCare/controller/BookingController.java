package com.example.Dr.VehicleCare.controller;

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
import com.example.Dr.VehicleCare.security.JwtService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(
    origins = {
        "http://localhost:3000",
        "https://www.drvehiclecare.com"
    }
)
public class BookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BikeCompanyRepository bikeCompanyRepository;
    private final BikeModelRepository bikeModelRepository;
    private final CustomizedServiceRepository customizedServiceRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    public BookingController(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BikeCompanyRepository bikeCompanyRepository,
            BikeModelRepository bikeModelRepository,
            CustomizedServiceRepository customizedServiceRepository,
            EmailService emailService,
            JwtService jwtService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.bikeCompanyRepository = bikeCompanyRepository;
        this.bikeModelRepository = bikeModelRepository;
        this.customizedServiceRepository = customizedServiceRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    // ================= CREATE BOOKING =================
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking booking = new Booking();

            booking.setUser(user);
            booking.setBikeCompany(
                    bikeCompanyRepository.findById(req.getBikeCompanyId()).orElse(null)
            );
            booking.setBikeModel(
                    bikeModelRepository.findById(req.getBikeModelId()).orElse(null)
            );
            booking.setServiceType(ServiceType.valueOf(req.getServiceType()));
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
            booking.setPaymentStatus(PaymentStatus.UNPAID);

            // ========= CUSTOMIZED SERVICE =========
            if ("CUSTOMIZED".equalsIgnoreCase(req.getServiceType())
                    && req.getCustomizedService() != null) {

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

            Booking savedBooking = bookingRepository.save(booking);

            emailService.notifyAdminBooking(
                    "New booking from " + user.getName() + " (" + user.getPhone() + ")"
            );

            return ResponseEntity.ok(savedBooking);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create booking");
        }
    }

    // ================= GET MY BOOKINGS =================
    @GetMapping("/my")
    public ResponseEntity<?> myBookings(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        return ResponseEntity.ok(bookingRepository.findByUserId(userId));
    }

    // ================= GET BOOKING BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================= DELETE BOOKING =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }
}
