package com.example.Dr.VehicleCare.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.service.JwtService;
import com.example.Dr.VehicleCare.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

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
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {

        try {
            Long bookingId = Long.valueOf(data.get("bookingId").toString());
            BigDecimal amount = new BigDecimal(data.get("amount").toString()); // RUPEES ONLY

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Razorpay API may throw RazorpayException
            Order order = paymentService.createOrder(booking, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("id", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));

            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to create payment order", "message", e.getMessage())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("error", "Internal server error", "message", e.getMessage())
            );
        }
    }

    // ✅ VERIFY PAYMENT
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            String token = authHeader.substring(7);
            jwtService.extractUserId(token); // validate token

            Long bookingId = Long.parseLong(request.get("bookingId"));
            String orderId = request.get("razorpay_order_id");
            String paymentId = request.get("razorpay_payment_id");
            String signature = request.get("razorpay_signature");

            boolean verified = paymentService.verifyPayment(orderId, paymentId, signature);

            if (!verified) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Payment verification failed"));
            }

            return ResponseEntity.ok(Map.of("message", "Payment verified successfully"));

        } catch (RazorpayException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Payment verification failed", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
