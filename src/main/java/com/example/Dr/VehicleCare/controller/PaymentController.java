package com.example.Dr.VehicleCare.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.service.JwtService;
import com.example.Dr.VehicleCare.service.PaymentService;
import com.razorpay.Order;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final JwtService jwtService;

    public PaymentController(
            PaymentService paymentService,
            BookingRepository bookingRepository,
            JwtService jwtService
    ) {
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
        this.jwtService = jwtService;
    }

    // ✅ CREATE RAZORPAY ORDER
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) throws Exception {

        Long bookingId = Long.valueOf(data.get("bookingId").toString());
        BigDecimal amount = new BigDecimal(data.get("amount").toString())
                .multiply(BigDecimal.valueOf(100)); // ✅ convert to paise

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Order order = paymentService.createOrder(booking, amount);

        // ✅ RETURN JSON (NOT STRING)
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));

        return ResponseEntity.ok(response);
    }

    // ✅ VERIFY PAYMENT
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        jwtService.extractUserId(token); // just validate token

        Long bookingId = Long.parseLong(request.get("bookingId"));

        String razorpayOrderId = request.get("razorpay_order_id");
        String razorpayPaymentId = request.get("razorpay_payment_id");
        String razorpaySignature = request.get("razorpay_signature");

        // ✅ VERIFY SIGNATURE
        paymentService.verifySignature(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(booking);

        return ResponseEntity.ok("Payment verified successfully");
    }
}
