package com.example.Dr.VehicleCare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomizedServiceDTO {
    private String bikeCompany;
    private String bikeModel;
    private int cc;
    private boolean wash;
    private boolean oilChange;
    private boolean chainLube;
    private boolean engineTuneUp;
    private boolean breakCheck;
    private boolean fullbodyPolishing;
    private boolean generalInspection;
    private double totalPrice;
}
