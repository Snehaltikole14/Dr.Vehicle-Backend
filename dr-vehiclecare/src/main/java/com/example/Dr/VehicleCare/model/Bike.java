package com.example.Dr.VehicleCare.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bikes")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private BikeCompany company;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private BikeModel model;

    public String getModelName() {
        if (company != null && model != null) {
            return company.getName() + " " + model.getModelName();
        } else if (model != null) {
            return model.getModelName();
        } else if (company != null) {
            return company.getName();
        } else {
            return "Unknown Bike";
        }
    }
}
