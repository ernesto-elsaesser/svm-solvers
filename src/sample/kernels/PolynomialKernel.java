package sample.kernels;

public class PolynomialKernel implements Kernel {

    public double apply(double[] x1, double[] x2) {
        double prod = 0;
        for(int i = 0; i < x2.length; i++) {
            double t  = 1 + (x1[i] * x2[i]);
            prod += Math.pow(t, 2);
        }
        return prod;
    }
}
