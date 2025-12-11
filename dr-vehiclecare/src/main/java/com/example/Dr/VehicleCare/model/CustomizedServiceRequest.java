package com.example.Dr.VehicleCare.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customized_service")
public class CustomizedServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

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
