package com.example.Dr.VehicleCare.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.Dr.VehicleCare.model.enums.BookingStatus;
import com.example.Dr.VehicleCare.model.enums.PaymentStatus;
import com.example.Dr.VehicleCare.model.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who is booking
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // Bike Company
    @ManyToOne
    @JoinColumn(name = "bike_company_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BikeCompany bikeCompany;

    // Bike Model
    @ManyToOne
    @JoinColumn(name = "bike_model_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BikeModel bikeModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;
    
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customized_service_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CustomizedServiceRequest customizedService;

    
    private Double servicePrice;



    // Appointment Date
    @Column(nullable = false)
    private LocalDate appointmentDate;

    // Full Address
    @Column(nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    private String city;
    private String pincode;
    private String landmark;

    private Double latitude;
    private Double longitude;

    // Booking status
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    // Assigned technician (optional)
    @ManyToOne
    @JoinColumn(name = "technician_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User technician;

    // Payment status
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    // Notes
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Timestamp
    private LocalDateTime createdAt = LocalDateTime.now();

     @Column(name = "time_slot", nullable = false)
    private String timeSlot;
   
}

