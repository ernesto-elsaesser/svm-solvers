package sample;

import java.util.ArrayList;
import java.util.List;

public class SVM {

    public static final double EPSILON = 1e-3;

    List<SupportVector> vectors = new ArrayList<>();
    double b = 0; // treshold
    double c = 1.0; // soft-margin parameter

    public void add(double[] vector, int y) {
        vectors.add(new SupportVector(vector, y));
    }

    public double output(double[] x) {
        // $u = \sum_j \alpha_j y_j K(x_j, x) - b$
        double u = -b;
        for(SupportVector v : vectors) {
            if(v.alpha <= EPSILON)
                continue; // ignore non-support vectors
            u += v.alpha * v.y * kernelFunc(v.x, x);
        }
        return u;
    }

    public double kernelFunc(double[] x1, double[] x2) {
        // simple dot product = linear kernel
        double prod = 0;
        for(int i = 0; i < x2.length; i++)
            prod += x1[i] * x2[i];
        return prod;
    }
}