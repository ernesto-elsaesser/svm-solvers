package sample;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

public class DualityProblem {
    public static void main(String[] args) throws Exception {
        ArrayRealVector s1 = new ArrayRealVector(new double[]{4, 2});
        ArrayRealVector s2 = new ArrayRealVector(new double[]{2, 5});
        ArrayRealVector s3 = new ArrayRealVector(new double[]{3, 8});

        int[] Y = {-1, -1, 1};

        RealVector nv1 = s1.append(1);
        RealVector nv2 = s2.append(1);
        RealVector nv3 = s3.append(1);

        RealVector[] x = new RealVector[] {nv1, nv2, nv3};

        double[] aSolution = new double[] {-0.68, 13.65, 8.42};
        System.out.println(Arrays.toString(aSolution) + " : " + dualProblem(aSolution, Y, x));
        System.out.println("-------------------------------------");

        double a[] = new double[3];

        double max[] = null;
        double maxL = Double.MIN_NORMAL;

        for (int i = 0; i < 1000; i++) {
                a[0] = Math.random() * 50;
                a[1] = Math.random() * 50;
                a[2] = a[0] + a[1]; // just works because of your Y => assumption (sum(ai * yi)=0)

            double L = dualProblem(a, Y, x);
            System.out.println(Arrays.toString(a) + " : " + L);

            if (L > maxL) {
                max = a;
                maxL = L;
            }
        }

        System.out.println("BEST");
        System.out.println(Arrays.toString(max) + " : " + maxL);
    }

    public static double dualProblem(double[] a, int[] y, RealVector[] x) throws Exception {
        if(!((a.length == y.length) && (a.length == x.length)))
            throw new Exception("length must match!");

        int n = a.length;

        double alphaSum = DoubleStream.of(a).sum();

        double outerSum = 0;
        for (int i = 0; i < n; i++) {
            double innerSum = 0;

            for (int j = 0; j < n; j++)
                innerSum += y[i] * y[j] * a[i] * a[j] + x[i].dotProduct(x[j]);

            outerSum += innerSum;
        }

        return alphaSum - (1d/2d) * outerSum;
    }
}
