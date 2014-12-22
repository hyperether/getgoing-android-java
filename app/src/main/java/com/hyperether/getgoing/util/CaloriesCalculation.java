package com.hyperether.getgoing.util;

import com.hyperether.getgoing.data.CBDataFrame;


public class CaloriesCalculation {
    // Algorithm variables
    // Lookup table for calories calculation first is speed in m/s, second is
    // kcal/m*kg
    private final double kcalMatrix[][] = {{0.89, 0.00078}, {1.12, 0.00074},
            {1.34, 0.00073}, {1.56, 0.00071}, {1.79, 0.00078},
            {2.01, 0.00087}, {2.23, 0.00099}, {2.68, 0.00104},
            {3.13, 0.00102}, {3.58, 0.00105}, {4.02, 0.00104},
            {4.47, 0.00112}};

    public double calculate(double dis, double vel, CBDataFrame cbDataFrameLocal, double weight) {

        double energySpent = 0;
        int i;

        if (cbDataFrameLocal.getMeasurementSystemId() == 1 || cbDataFrameLocal.getMeasurementSystemId() == 2)
            weight = weight * 0.4536; //convert weght to metric system for calculations;

        if (cbDataFrameLocal.getProfileId() == 1 || cbDataFrameLocal.getProfileId() == 2) {
            // walking and running algorithm

            if (vel < kcalMatrix[0][0]) { // if the measured speed is lower than the
                // first entry use the first entry
                energySpent = dis * (kcalMatrix[0][1] * weight);
            } else if (vel > kcalMatrix[11][0]) { // if the measured speed is higher
                // than the last entry use the
                // last entry
                energySpent = dis * (kcalMatrix[11][1] * weight);
            } else
                for (i = 1; i < 12; i++) {
                    if (vel < kcalMatrix[i][0]) { // take the next higher value
                        energySpent = dis * (kcalMatrix[i][1] * weight);
                        break;
                    }
                }
        } else {    // bicycle riding is selected
            if (vel < 4.47) {
                energySpent = dis * (0.000779 * weight);    // riding velocity lower than 16 km/h
            } else {
                energySpent = dis * (0.001071 * weight);    // riding velocity higher than 16 km/h
            }
        }

        return energySpent;
    }
}
