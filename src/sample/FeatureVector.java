package sample;

public class FeatureVector {

    public final double[] x;
    public final int y;
    public final int altY;
    public double alpha = 0;

    /** [SMO only] is the Lagrange multiplier bound? */
    public transient boolean bound = true;

    public FeatureVector(double x1, double x2, int y) {
        this(new double[] {x1, x2}, y);
    }

    public FeatureVector(double[] x, int y) {
        this.x = x;
        this.y = y >= 1 ? 1 : -1;
        this.altY = y >= 1 ? 1 : 0;
    }
}