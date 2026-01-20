package com.example.Dr.VehicleCare.service;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.Payment;
import com.example.Dr.VehicleCare.model.enums.PaymentMethod;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

@Service
public class PaymentService {

    @Value("${razorpay.key}")
    private String keyId;

    @Value("${razorpay.secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    // ================= CREATE ORDER =================
    public Order createOrder(Booking booking, BigDecimal amount) throws RazorpayException {

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        options.put("amount", amount.multiply(BigDecimal.valueOf(100))); // convert to paise
        options.put("currency", "INR");
        options.put("receipt", "booking_" + booking.getId());

        Order order = client.orders.create(options);

        // Save payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(PaymentMethod.UPI);
        payment.setRazorpayOrderId(order.get("id"));
        payment.setStatus(PaymentStatus.UNPAID); // default
        paymentRepository.save(payment);

        // Make sure booking initially marked UNPAID
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        bookingRepository.save(booking);

        return order;
    }

    // ================= VERIFY PAYMENT =================
    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature) throws RazorpayException {

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId);

        if (payment == null) {
            throw new RuntimeException("Payment record not found for orderId: " + orderId);
        }

        String payload = orderId + "|" + paymentId;

        boolean isValid = Utils.verifySignature(payload, signature, keySecret);

        if (isValid) {
            // Update payment
            payment.setStatus(PaymentStatus.PAID);
            payment.setRazorpayPaymentId(paymentId);
            payment.setRazorpaySignature(signature);
            paymentRepository.save(payment);

            // Update booking as well
            Booking booking = payment.getBooking();
            booking.setPaymentStatus(PaymentStatus.PAID);
            bookingRepository.save(booking);

            return true;
        }

        // If verification fails
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        // Booking remains UNPAID if verification fails
        return false;
    }
}
