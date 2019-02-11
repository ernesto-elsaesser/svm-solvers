package sample;

public class SupportVector {

    /** The input vector */
    final double[] x;
    /** The target class: either +1 or -1 */
    final byte y;
    /** The Lagrange multiplier for this example */
    double alpha;
    /** Is the Lagrange multiplier bound? */
    transient boolean bound = true;

    public SupportVector(double x1, double x2, int y) {
        this(new double[] {x1, x2}, y, 0);
    }

    public SupportVector(double x1, double x2, double x3, double x4, int y) {
        this(new double[] {x1, x2, x3, x4}, y, 0);
    }

    public SupportVector(double[] x, int y, double alpha) {
        this.x = x;
        this.alpha = alpha;
        if (y >= 1) {
            this.y = (byte)1;
        } else {
            this.y = (byte)0;
        }
    }

    public double sign() {
        return y == 0 ? -1.0 : 1.0;
    }

    public SupportVector clone(double newAlpha) {
        return new SupportVector(x, y , newAlpha);
    }
}