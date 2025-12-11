package com.example.Dr.VehicleCare.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.Dr.VehicleCare.model.Otp;
import com.example.Dr.VehicleCare.repository.OtpRepository;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

    @Value("${fast2sms.apiKey}")
    private String apiKey;

    public String generateOtp(String email, String phoneNumber) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        // Send SMS
        sendSms(phoneNumber, otpCode);

        // Send Email
        emailService.sendSimpleMessage(
            email,
            "Your Dr.VehicleCare OTP Code",
            "<h3>Your OTP Code is: <b>" + otpCode + "</b></h3><p>This code will expire in 5 minutes.</p>"
        );

        return otpCode;
    }

    private void sendSms(String phoneNumber, String otpCode) {
        String message = "Your OTP for Dr.VehicleCare is " + otpCode;
        String url = "https://www.fast2sms.com/dev/bulkV2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", apiKey);

        Map<String, Object> body = Map.of(
                "route", "v3",
                "sender_id", "TXTIND",
                "message", message,
                "language", "english",
                "flash", 0,
                "numbers", phoneNumber
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("SMS sent response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Error sending SMS: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String code) {
        return otpRepository.findByEmailAndCode(email, code)
                .filter(o -> o.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(o -> {
                    otpRepository.delete(o);
                    return true;
                }).orElse(false);
    }
}
