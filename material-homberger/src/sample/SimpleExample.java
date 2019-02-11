package sample;

import org.apache.commons.math3.linear.*;

import java.util.Arrays;

public class SimpleExample {
    public static void main(String[] args) {

        // our 3 vectors with additional dimension
        ArrayRealVector s1 = new ArrayRealVector(new double[]{4, 2});
        ArrayRealVector s2 = new ArrayRealVector(new double[]{2, 5});
        ArrayRealVector s3 = new ArrayRealVector(new double[]{3, 8});

        double[] vectorClassification = {-1, -1, 1};

        RealVector nv1 = s1.append(1);
        RealVector nv2 = s2.append(1);
        RealVector nv3 = s3.append(1);

        double a11 = nv1.dotProduct(nv1);
        double a21 = nv1.dotProduct(nv2);
        double a31 = nv1.dotProduct(nv3);

        double a12 = nv2.dotProduct(nv1);
        double a22 = nv2.dotProduct(nv2);
        double a32 = nv2.dotProduct(nv3);

        double a13 = nv3.dotProduct(nv1);
        double a23 = nv3.dotProduct(nv2);
        double a33 = nv3.dotProduct(nv3);

        RealMatrix coefficients =
                new Array2DRowRealMatrix(new double[][]{
                        {a11, a21, a31},
                        {a12, a22, a32},
                        {a13, a23, a33}},
                        false);

        System.out.println("LGS:");
        printMatrix(coefficients);

        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        RealVector constants = new ArrayRealVector(vectorClassification, false);

        RealVector solution = solver.solve(constants);
        System.out.println("Solution");
        printVector(solution);

        RealVector wh = nv1.mapMultiply(
                solution.getEntry(0)
        ).add(nv2.mapMultiply(
                solution.getEntry(1))
        ).add(nv3.mapMultiply(
                solution.getEntry(2)
        ));

        RealVector w = wh.getSubVector(0,2);
        System.out.println("w =");
        printVector(w);

        double b = w.dotProduct(s1) / vectorClassification[0];

        System.out.println("b = " + b);
    }

    private static void printMatrix(RealMatrix coefficients) {
        for (int i = 0; i < coefficients.getRowDimension(); i++) {
            System.out.println(Arrays.toString(coefficients.getRow(i)));
        }
    }

    private static void printVector(RealVector solution) {
        System.out.println(Arrays.toString(solution.toArray()));
    }
}
