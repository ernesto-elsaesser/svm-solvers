package sample;

public class SupportVector {

    /** The input vector */
    final double[] x;
    /** The target class: either +1 or -1 */
    final byte y;
    /** The Lagrange multiplier for this example */
    double alpha = 0;
    /** Is the Lagrange multiplier bound? */
    transient boolean bound = true;

    public SupportVector(double x1, double x2, byte y) {
        double[] x = {x1, x2};
        this.x = x;
        this.y = y;
    }

    public double sign() {
        return y == 0 ? -1.0 : 1.0;
    }
}