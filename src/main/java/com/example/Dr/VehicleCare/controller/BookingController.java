package com.example.Dr.VehicleCare.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.dto.BookingRequest;
import com.example.Dr.VehicleCare.dto.CustomizedServiceDTO;
import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.CustomizedServiceRequest;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.BookingStatus;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.model.enums.ServiceType;
import com.example.Dr.VehicleCare.repository.BikeCompanyRepository;
import com.example.Dr.VehicleCare.repository.BikeModelRepository;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.repository.CustomizedServiceRepository;
import com.example.Dr.VehicleCare.repository.UserRepository;
import com.example.Dr.VehicleCare.service.EmailService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Long userId = Long.parseLong(principal.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = new Booking();

        // Set base data
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
        booking.setLatitude(req.getLatitude());
        booking.setLongitude(req.getLongitude());
        booking.setNotes(req.getNotes());
        booking.setStatus(BookingStatus.PENDING);

        // ===================== CUSTOMIZED SERVICE LOGIC =====================
        if (req.getServiceType().equalsIgnoreCase("CUSTOMIZED") &&
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

            // Set total price in booking
            booking.setServicePrice(dto.getTotalPrice());
        } else {
            // For normal services, get price from request
            booking.setServicePrice(req.getServicePrice());
        }

        // Payment status initially UNPAID
        booking.setPaymentStatus(PaymentStatus.UNPAID);

        // SAVE BOOKING
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
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Long userId = Long.parseLong(principal.getName());
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
