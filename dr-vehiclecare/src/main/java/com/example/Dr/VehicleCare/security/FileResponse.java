package com.example.Dr.VehicleCare.security;



import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class FileResponse {
    private String absolutePath;
    private String contentType;
}

