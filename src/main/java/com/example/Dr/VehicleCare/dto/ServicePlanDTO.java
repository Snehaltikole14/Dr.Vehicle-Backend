package com.example.Dr.VehicleCare.dto;

import java.util.List;

import lombok.Data;

@Data
public class ServicePlanDTO {
    private Long id;
    private String title;
    private String price;
    private List<String> features;
    private boolean highlight;
}
