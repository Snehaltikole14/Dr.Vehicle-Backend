package com.example.Dr.VehicleCare.service;



import com.example.Dr.VehicleCare.model.Payment;
import com.example.Dr.VehicleCare.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}

