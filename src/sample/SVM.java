package sample;

import java.util.ArrayList;
import java.util.List;

public class SVM {

    public boolean dataIsLinearilySeparatable = false;
    List<SupportVector> vectors;
    double b = 0; // treshold
    double c = 1.0; // soft-margin parameter

    public double dotKernel(double[] x1, double[] x2) {
        double prod = 0;
        for(int i = 0; i < x2.length; i++)
            prod += x1[i] * x2[i];
        return prod;
    }

    public double polyKernel(double[] x1, double[] x2) {
        double prod = 0;
        for(int i = 0; i < x2.length; i++) {
            double t  = 1 + (x1[i] * x2[i]);
            prod += Math.pow(t, 2);
        }
        return prod;
    }
}