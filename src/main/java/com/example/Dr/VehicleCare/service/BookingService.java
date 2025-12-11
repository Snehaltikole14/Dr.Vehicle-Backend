package com.example.Dr.VehicleCare.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Dr.VehicleCare.model.Booking;
import com.example.Dr.VehicleCare.model.CustomizedServiceRequest;
import com.example.Dr.VehicleCare.repository.BookingRepository;
import com.example.Dr.VehicleCare.repository.CustomizedServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomizedServiceRepository customizedServiceRepository;
    private final EmailService emailService;

    @Transactional
    public Booking saveBooking(Booking booking) {

        // If booking has a customized service, save it first
        CustomizedServiceRequest csr = booking.getCustomizedService();
        if (csr != null) {
            CustomizedServiceRequest savedCsr = customizedServiceRepository.save(csr);
            booking.setCustomizedService(savedCsr);
        }

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        // Prepare bike info
        String companyName = (booking.getBikeCompany() != null) ? booking.getBikeCompany().getName() : "Unknown Company";
        String modelName = (booking.getBikeModel() != null) ? booking.getBikeModel().getModelName() : "Unknown Model";

        // Notify admin
        String details = "New booking:\n" +
                         "Name: " + booking.getUser().getName() + "\n" +
                         "Phone: " + booking.getUser().getPhone() + "\n" +
                         "Date: " + booking.getAppointmentDate() + "\n" +
                         "Bike: " + companyName + " " + modelName;

        emailService.notifyAdminBooking(details);

        return savedBooking;
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }
}
