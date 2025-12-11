package com.example.Dr.VehicleCare.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bike_models")
public class BikeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelName;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({"models"})  // Ignore the "models" field in BikeCompany during serialization
    private BikeCompany company;


    private Integer engineCc;
    private Double price;
    private String imageUrl;
}
