package sample;

public class FeatureVector {

    public final double[] x;
    public final int y;
    public double alpha = 0;

    public FeatureVector(double x1, double x2, int y) {
        this(new double[] {x1, x2}, y);
    }

    public FeatureVector(double[] x, int y) {
        this.x = x;
        this.y = y >= 1 ? 1 : -1;
    }
}