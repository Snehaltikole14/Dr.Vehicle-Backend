package com.example.Dr.VehicleCare.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.Utils;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;

    @Value("${razorpay.key}")
    private String razorpayKeyId;

    @Value("${razorpay.secret}")
    private String razorpayKeySecret;

    public PaymentController(PaymentService paymentService,
                             BookingRepository bookingRepository) {
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
    }

    // ✅ Send key to frontend (avoid hardcode)
    @GetMapping("/key")
    public ResponseEntity<?> getKey() {
        return ResponseEntity.ok(Map.of("key", razorpayKeyId));
    }

    // ✅ CREATE RAZORPAY ORDER
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) throws Exception {

        Long bookingId = Long.valueOf(data.get("bookingId").toString());

        // amount should come in PAISE
        BigDecimal amount = new BigDecimal(data.get("amount").toString());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Order order = paymentService.createOrder(booking, amount);

        JSONObject json = order.toJson();

        return ResponseEntity.ok(Map.of(
                "id", json.getString("id"),
                "amount", json.get("amount"),
                "currency", json.getString("currency")
        ));
    }

    // ✅ VERIFY PAYMENT (REAL VERIFICATION)
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {

        String bookingIdStr = request.get("bookingId");
        String razorpayOrderId = request.get("razorpay_order_id");
        String razorpayPaymentId = request.get("razorpay_payment_id");
        String razorpaySignature = request.get("razorpay_signature");

        if (bookingIdStr == null || razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            return ResponseEntity.badRequest().body("Missing payment parameters");
        }

        Long bookingId = Long.parseLong(bookingIdStr);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        try {
            // ✅ signature verification
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                booking.setPaymentStatus(PaymentStatus.FAILED);
                bookingRepository.save(booking);
                return ResponseEntity.status(400).body("Invalid payment signature");
            }

            // ✅ success
            booking.setPaymentStatus(PaymentStatus.PAID);
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of(
                    "message", "Payment verified successfully",
                    "bookingId", booking.getId(),
                    "paymentId", razorpayPaymentId
            ));

        } catch (Exception e) {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            return ResponseEntity.status(500).body("Payment verification error: " + e.getMessage());
        }
    }
}
