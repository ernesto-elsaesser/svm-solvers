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

    public SupportVector(double[] vector, int y) {
        if(Math.abs(y) != 1)
            throw new IllegalArgumentException("y must be either +1 or -1");
        this.x = vector;
        this.y = (byte) y;
    }
}