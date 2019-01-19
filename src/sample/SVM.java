package sample;

import java.util.ArrayList;
import java.util.List;

public class SVM {

    interface Kernel {
        double getValue(double[] x1, double[] x2);
    }

    class LinearKernel implements Kernel {
        public double getValue(double[] x1, double[] x2) {
            double prod = 0;
            for(int i = 0; i < x2.length; i++)
                prod += x1[i] * x2[i];
            return prod;
        }
    }

    public static final double EPSILON = 1e-3;
    /**
     * The support vectors
     */
    List<SupportVector> vectors = new ArrayList<>();
    /**
     * The threshold
     */
    double b = 0;
    /**
     * The kernel function
     */
    Kernel kernel;
    /**
     * The soft-margin parameter
     */
    double c;

    public SVM() {
        this.kernel = new LinearKernel();
        this.c = 1.0;
    }

    public void add(double[] vector, int y) {
        vectors.add(new SupportVector(vector, y));
    }

    public double output(double[] x) {
        // $u = \sum_j \alpha_j y_j K(x_j, x) - b$
        double u = -b;
        for(SupportVector v : vectors) {
            if(v.alpha <= EPSILON)
                continue; // ignore non-support vectors
            u += v.alpha * v.y * kernel.getValue(v.x, x);
        }
        return u;
    }
}