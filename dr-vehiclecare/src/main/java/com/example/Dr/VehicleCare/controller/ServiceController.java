package com.example.Dr.VehicleCare.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.dto.ServicePlanDTO;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ServiceController {

	
	

	    @GetMapping("/plans")
	    public List<ServicePlanDTO> getPlans() {
	        List<ServicePlanDTO> list = new ArrayList<>();

	        ServicePlanDTO plan1 = new ServicePlanDTO();
	        plan1.setId(1L);
	        plan1.setTitle("Up to 100cc-160cc");
	        plan1.setPrice("₹849");
	        plan1.setFeatures(List.of(
	                "Engine Oil (10W30 or 20W40) – 1L",
	                "Air Filter Cleaning",
	                "Spark Plug Cleaning",
	                "Carburetor Cleaning & Adjustment",
	                "Brake Cleaning & Adjustment",
	                "Horn Setting",
	                "Tyre Pressure Check",
	                "Minor Fittings",
	                "Oiling & Greasing",
	                "Fork Settings",
	                "Shock Absorber Check-Up",
	                "Electrical Check-Up",
	                "Drive Chain Adjustment",
	                "Foam Wash"
	        ));
	        plan1.setHighlight(false);

	        ServicePlanDTO plan2 = new ServicePlanDTO();
	        plan2.setId(2L);
	        plan2.setTitle("Above 180cc");
	        plan2.setPrice("₹999");
	        plan2.setFeatures(plan1.getFeatures());
	        plan2.setHighlight(true);

	        list.add(plan1);
	        list.add(plan2);
	        return list;
	    }
	}


