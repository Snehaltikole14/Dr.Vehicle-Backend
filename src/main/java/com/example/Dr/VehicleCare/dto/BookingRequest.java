package com.example.Dr.VehicleCare.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class BookingRequest {

    private Long bikeCompanyId;
    private Long bikeModelId;

    private String serviceType;
    private String appointmentDate;

    private String fullAddress;
    private String city;
    private String pincode;
    private String landmark;

    private Double latitude;
    private Double longitude;

    private String notes;
    private Double servicePrice;
    private CustomizedServiceDTO customizedService;
       private String timeSlot;


}



