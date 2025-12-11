package com.example.Dr.VehicleCare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.model.Booking;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${admin.email:tikolesnehal@gmail.com}")
    private String adminEmail;

    @Value("${spring.mail.username:no-reply@drvehiclecare.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ==================== REUSABLE EMAIL SENDER ====================
    private void sendEmail(String to, String subject, String htmlContent, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, isHtml);

            mailSender.send(message);
            logger.info("✅ Email sent successfully to {}", to);

        } catch (MessagingException e) {
            logger.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ==================== OTP EMAIL ====================
    public void sendOtpEmail(String to, String otp) {
        String html = """
            <div style='font-family: Arial, sans-serif; color: #333;'>
                <h2 style='color:#007bff;'>Dr. Vehicle Care Verification</h2>
                <p>Dear user,</p>
                <p>Your OTP code is:</p>
                <h3 style='background:#f3f3f3; display:inline-block; padding:10px 20px; border-radius:8px;'>%s</h3>
                <p>This code will expire in <b>5 minutes</b>. Do not share it with anyone.</p>
                <p>Thank you,<br/>The Dr. Vehicle Care Team</p>
            </div>
        """.formatted(otp);

        sendEmail(to, "Your OTP Code - Dr. Vehicle Care", html, true);
    }

    // ==================== PASSWORD RESET EMAIL ====================
    public void sendPasswordResetEmail(String to, String resetLink) {
        String html = """
            <div style='font-family: Arial, sans-serif; color: #333;'>
                <h2 style='color:#007bff;'>Password Reset Request</h2>
                <p>We received a request to reset your password.</p>
                <p>Click the button below to reset your password:</p>
                <p>
                    <a href='%s' style='background:#007bff; color:#fff; padding:10px 20px; text-decoration:none; border-radius:5px;'>
                        Reset Password
                    </a>
                </p>
                <p>If you didn’t request this, please ignore this email.</p>
                <p>Thanks,<br/>The Dr. Vehicle Care Team</p>
            </div>
        """.formatted(resetLink);

        sendEmail(to, "Reset Your Password - Dr. Vehicle Care", html, true);
    }

    // ==================== BOOKING NOTIFICATION ====================
    public void sendBookingNotification(String to, String userName, String bikeModel, String serviceType, String date) {
        String html = """
            <h3>New Booking Received</h3>
            <p>User: %s</p>
            <p>Bike: %s</p>
            <p>Service: %s</p>
            <p>Appointment Date: %s</p>
        """.formatted(userName, bikeModel, serviceType, date);

        sendEmail(to, "New Bike Service Booking", html, true);
    }

    // ==================== SIMPLE MESSAGE ====================
    public void sendSimpleMessage(String to, String subject, String body) {
        sendEmail(to, subject, body, true);
    }

    // ==================== ADMIN BOOKING ALERT ====================
    public void notifyAdminBooking(String details) {
        sendEmail(adminEmail, "New Booking Notification", details, true);
    }

    
    public void sendBookingFullDetails(String to, Booking booking) {
        String userName = booking.getUser().getName() + " (" + booking.getUser().getPhone() + ")";
        String bikeModel = booking.getBikeModel() != null ? booking.getBikeModel().getModelName() : "Unknown Bike";
        String bikeCompany = booking.getBikeModel() != null && booking.getBikeModel().getCompany() != null
                ? booking.getBikeModel().getCompany().getName() : "Unknown Company";
        String serviceType = booking.getServiceType().name();
        String appointmentDate = booking.getAppointmentDate().toString();
        String fullAddress = booking.getFullAddress();
        String city = booking.getCity() != null ? booking.getCity() : "-";
        String pincode = booking.getPincode() != null ? booking.getPincode() : "-";
        String landmark = booking.getLandmark() != null ? booking.getLandmark() : "-";

        // Plain-text content (used for Gmail preview & fallback)
        String plainText = String.format(
            "New booking received from %s\nBike: %s (%s)\nService: %s\nDate: %s\nAddress: %s, %s, %s, Landmark: %s",
            userName, bikeModel, bikeCompany, serviceType, appointmentDate, fullAddress, city, pincode, landmark
        );

        // HTML content
        String html = """
            <div style='font-family: Arial, sans-serif; color: #333; line-height:1.5;'>
                <h2 style='color:#007bff;'>New Bike Service Booking</h2>
                <p><strong>User:</strong> %s</p>
                <p><strong>Bike Company:</strong> %s</p>
                <p><strong>Bike Model:</strong> %s</p>
                <p><strong>Service Type:</strong> %s</p>
                <p><strong>Appointment Date:</strong> %s</p>
                <p><strong>Full Address:</strong> %s</p>
                <p><strong>City:</strong> %s</p>
                <p><strong>Pincode:</strong> %s</p>
                <p><strong>Landmark:</strong> %s</p>
                <hr style='border:1px solid #ccc;'/>
                <p style='font-size:0.9rem; color:#555;'>This is an automated notification from <b>Dr. Vehicle Care</b>.</p>
            </div>
        """.formatted(userName, bikeCompany, bikeModel, serviceType, appointmentDate, fullAddress, city, pincode, landmark);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("Dr. Vehicle Care <official@drvehiclecare.com>");
            helper.setTo(to);
            helper.setSubject("New Booking Notification - Dr. Vehicle Care");

            // Important: set both plain text and HTML
            helper.setText(plainText, html);

            mailSender.send(message);
            logger.info("✅ Booking email sent successfully to {}", to);
        } catch (MessagingException e) {
            logger.error("❌ Failed to send booking email to {}: {}", to, e.getMessage());
        }
    }

    
    public String getAdminEmail() {
        return adminEmail;
    }
}
