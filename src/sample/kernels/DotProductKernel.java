package sample.kernels;

public class DotProductKernel implements Kernel {

    public double apply(double[] x1, double[] x2) {
        double prod = 0;
        for(int i = 0; i < x2.length; i++)
            prod += x1[i] * x2[i];
        return prod;
    }
}
