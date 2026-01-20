package com.example.Dr.VehicleCare.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.service.JwtService;
import com.example.Dr.VehicleCare.service.PaymentService;

import com.razorpay.Order;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final JwtService jwtService;

    public PaymentController(PaymentService paymentService,
                             BookingRepository bookingRepository,
                             JwtService jwtService) {
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
        this.jwtService = jwtService;
    }

    // CREATE RAZORPAY ORDER
    @PostMapping("/create-order")
    public String createOrder(@RequestBody Map<String, Object> data) throws Exception {

        Long bookingId = Long.valueOf(data.get("bookingId").toString());
        BigDecimal amount = new BigDecimal(data.get("amount").toString());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Order order = paymentService.createOrder(booking, amount);
        return order.toString();
    }

    // VERIFY PAYMENT
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader
    ) {
        // Extract token from header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String token = authHeader.substring(7);

        Long userId;
        try {
            userId = Long.parseLong(jwtService.extractUserId(token)); // ✅ Always initialize here
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        String bookingIdStr = request.get("bookingId");
        if (bookingIdStr == null) {
            return ResponseEntity.badRequest().body("Booking ID missing");
        }

        Long bookingId = Long.parseLong(bookingIdStr);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(booking);

        return ResponseEntity.ok("Payment verified successfully");
    }
    
    



}
