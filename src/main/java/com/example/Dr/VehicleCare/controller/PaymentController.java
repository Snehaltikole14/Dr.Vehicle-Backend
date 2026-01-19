package com.example.Dr.VehicleCare.controller;

import java.math.BigDecimal;
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

    /* ================= CREATE ORDER ================= */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> data,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            validateToken(authHeader);

            Long bookingId = Long.valueOf(data.get("bookingId").toString());

            // IMPORTANT: Amount must already be in paise
            BigDecimal amount = new BigDecimal(data.get("amount").toString());

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            Order order = paymentService.createOrder(booking, amount);

            // 🔥 RETURN EXACT FIELDS FRONTEND EXPECTS
            return ResponseEntity.ok(Map.of(
                    "id", order.get("id"),
                    "amount", order.get("amount"),
                    "currency", order.get("currency")
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to create order",
                    "message", e.getMessage()
            ));
        }
    }

    /* ================= VERIFY PAYMENT ================= */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            validateToken(authHeader);

            String orderId = request.get("razorpay_order_id");
            String paymentId = request.get("razorpay_payment_id");
            String signature = request.get("razorpay_signature");

            boolean verified = paymentService.verifyPayment(orderId, paymentId, signature);
            if (!verified) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment verification failed"));
            }

            // 🔥 FIND BOOKING USING ORDER ID (BEST PRACTICE)
            Booking booking = bookingRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setPaymentStatus(PaymentStatus.PAID);
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of("message", "Payment successful"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Verification failed",
                    "message", e.getMessage()
            ));
        }
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        jwtService.extractUserId(authHeader.substring(7));
    }
}
