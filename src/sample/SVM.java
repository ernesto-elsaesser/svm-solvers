package sample;

import java.util.List;

public class SVM {

    List<SupportVector> vectors;
    public boolean usePolyKernel;
    double b = 0;
    double epsilon = 0;

    SVM(boolean usePolyKernel) {
        this.usePolyKernel = usePolyKernel;
    }

    public double output(double[] x) {
        // $u = \sum_j \alpha_j y_j K(x_j, x) - b$
        double u = b;
        for(SupportVector v : vectors) {
            if(v.alpha < epsilon)
                continue; // ignore non-support vectors
            u += v.alpha * v.sign() * this.kernelFunc(v.x, x);
        }
        return u;
    }

    public double kernelFunc(double[] x1, double[] x2) {
        if(usePolyKernel) {
            return this.polyKernel(x1, x2);
        }
        else{
            return this.dotKernel(x1, x2);
        }
    }

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