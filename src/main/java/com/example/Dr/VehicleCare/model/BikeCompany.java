package com.example.Dr.VehicleCare.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bike_companies")
public class BikeCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String logoUrl;

    // One company can have many models
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<BikeModel> models;
}
