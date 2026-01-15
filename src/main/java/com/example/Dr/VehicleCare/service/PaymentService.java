package com.example.Dr.VehicleCare.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.model.Booking;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class PaymentService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    // ✅ CREATE ORDER
    public Order createOrder(Booking booking, BigDecimal amount) throws Exception {

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

        Map<String, Object> options = new HashMap<>();
        options.put("amount", amount.intValue()); // paise
        options.put("currency", "INR");
        options.put("receipt", "booking_" + booking.getId());

        return client.orders.create(options);
    }

    // ✅ VERIFY SIGNATURE
    public void verifySignature(
            String orderId,
            String paymentId,
            String signature
    ) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("razorpay_order_id", orderId);
            params.put("razorpay_payment_id", paymentId);
            params.put("razorpay_signature", signature);

            Utils.verifyPaymentSignature(params, razorpaySecret);
        } catch (Exception e) {
            throw new RuntimeException("Payment signature verification failed");
        }
    }
}
