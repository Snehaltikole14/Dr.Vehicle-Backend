package com.example.Dr.VehicleCare.service;

import org.springframework.stereotype.Service;

@Service
public class CustomizedServiceService {

    public double calculatePrice(int cc,
                                 boolean wash,
                                 boolean oilChange,
                                 boolean chainLube,
                                 boolean engineTuneUp,
                                 boolean breakCheck,
                                 boolean fullbodyPolishing,
                                 boolean generalInspection) {

        double total = 0;

        // ===== CC-based price slabs =====
        double washPrice;
        double oilPrice;
        double lubePrice;
        double tunePrice;
        double breakPrice;
        double polishPrice;
        double inspectionPrice;

        if (cc <= 150) {
            washPrice = 149;
            oilPrice = 299;
            lubePrice = 149;
            tunePrice = 399;
            breakPrice = 99;
            polishPrice = 349;
            inspectionPrice = 149;

        } else if (cc <= 250) {
            washPrice = 199;
            oilPrice = 349;
            lubePrice = 199;
            tunePrice = 499;
            breakPrice = 149;
            polishPrice = 449;
            inspectionPrice = 199;

        } else if (cc <= 500) {
            washPrice = 249;
            oilPrice = 449;
            lubePrice = 249;
            tunePrice = 699;
            breakPrice = 199;
            polishPrice = 549;
            inspectionPrice = 249;

        } else {
            washPrice = 299;
            oilPrice = 599;
            lubePrice = 299;
            tunePrice = 999;
            breakPrice = 249;
            polishPrice = 699;
            inspectionPrice = 299;
        }

        // ===== Add selected service prices =====
        if (wash) total += washPrice;
        if (oilChange) total += oilPrice;
        if (chainLube) total += lubePrice;
        if (engineTuneUp) total += tunePrice;
        if (breakCheck) total += breakPrice;
        if (fullbodyPolishing) total += polishPrice;
        if (generalInspection) total += inspectionPrice;

        return total;
    }
}
